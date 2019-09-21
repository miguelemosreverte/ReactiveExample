package transaction

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.kafka.scaladsl.Transactional
import akka.kafka.{ ProducerMessage, _ }
import akka.pattern.ask
import akka.stream.scaladsl.{ Keep, RestartSource, RunnableGraph, Sink }
import akka.stream.{ ActorMaterializer, KillSwitches, Materializer, UniqueKillSwitch }
import akka.util.Timeout
import introduction.ImperfectActor
import org.apache.kafka.clients.producer.ProducerRecord
import serialization.json.JsonSerializer

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }

object Transaction {

  def □[Input, Output](
    SOURCE_TOPIC: String,
    SINK_TOPIC:   String,
    algorithm:    Input => Future[Seq[Output]]
  )(implicit
    system: ActorSystem,
    consumer: ConsumerSettings[String, String],
    producer: ProducerSettings[String, String],
    decoder:  JsonSerializer[Input],
    encoder:  JsonSerializer[Output]
  ): RunnableGraph[(UniqueKillSwitch, Future[immutable.Seq[String]])] = {

    type Msg = ConsumerMessage.TransactionalMessage[String, String]

    implicit val mat: Materializer = ActorMaterializer()(system)
    implicit val ec: ExecutionContext = system.dispatcher
    import scala.concurrent.duration._

    val imperfectActor: ActorRef = system.actorOf(Props(new ImperfectActor()), "ImperfectActor")

    var message: Msg = null

    RestartSource.onFailuresWithBackoff(
      minBackoff   = 0.1.seconds,
      maxBackoff   = 1.seconds,
      randomFactor = 0.2
    ) { () =>
      Transactional
        .source(consumer, Subscriptions.topics(SOURCE_TOPIC))
        .mapAsync(1) { msg =>
          message = msg

          (imperfectActor ? msg.record.value) (Timeout(1.seconds))
            .mapTo[String]
            .map { a =>
              Right(a)
            }
            .recover {
              case e: Exception =>
                Left(e)
            }

        }.map {

          case Left(a) => throw new RuntimeException("Uh oh.. intentional exception. Let's restart the entire stream.")
          case Right(value) =>
            ProducerMessage.single(new ProducerRecord(SINK_TOPIC, message.record.key, value), message.partitionOffset)

        }
        .via(Transactional.flow(producer, transactionalId))
    }
      .viaMat(KillSwitches.single)(Keep.right)
      .collect {
        case a: ProducerMessage.Result[_, String, _] =>
          a.message.record.value

      }
      .toMat(Sink.seq[String])(Keep.both)

  }

  def transactionalId: String = java.util.UUID.randomUUID().toString
}
