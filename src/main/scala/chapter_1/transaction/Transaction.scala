package chapter_1.transaction

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Transactional
import akka.kafka.{ProducerMessage, _}
import akka.stream.scaladsl.{Keep, RestartSource, Sink}
import akka.stream.{ActorMaterializer, KillSwitches, Materializer, UniqueKillSwitch}
import org.apache.kafka.clients.producer.ProducerRecord
import serialization.json.JsonSerializer

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

object Transaction {


  def □[Input, Output](
                        SOURCE_TOPIC: String,
                        SINK_TOPIC: String,
                        algorithm: Input => Future[Seq[Output]]
                      )(implicit
                        system: ActorSystem,
                        consumer: ConsumerSettings[String, String],
                        producer: ProducerSettings[String, String],
                        decoder: JsonSerializer[Input],
                        encoder: JsonSerializer[Output]
                      ): (UniqueKillSwitch, Future[immutable.Seq[Seq[String]]]) = {

    type Msg = ConsumerMessage.TransactionalMessage[String, String]

    implicit val mat: Materializer = ActorMaterializer()(system)
    implicit val ec: ExecutionContext = system.dispatcher
    import scala.concurrent.duration._


    var message: Msg = null

    RestartSource.onFailuresWithBackoff(
      minBackoff = 0.1.seconds,
      maxBackoff = 1.seconds,
      randomFactor = 0.2
    ) { () =>
      Transactional
        .source(consumer, Subscriptions.topics(SOURCE_TOPIC))
        .mapAsync(1) { msg =>
          message = msg

          val input = message.record.value
          val decoded: Either[String, Input] = decoder.decode(input)
          decoded match {
            case Left(e) =>
              Future(Left(e))

            case Right(decoded) =>
              algorithm(decoded)
                .map(_ map {
                  encoder.encode
                })
                .map { a: Seq[String] =>
                  Right(a)
                }
                .recover {
                  case e: Exception =>
                    Left(e.getMessage)
                }

          }
        }.map {

        case Left(cause) => throw new RuntimeException(
          s"""
             Uh oh.. intentional exception. Let's restart the entire stream.


             ${Console.RED} $cause ${Console.RESET}

          """)
        case Right(output) =>
          ProducerMessage.multi(
            records = output.map { o =>
              new ProducerRecord(
                SINK_TOPIC,
                message.record.key,
                o
              )
            }.toList,
            passThrough = message.partitionOffset
          )

      }
        .via(Transactional.flow(producer, transactionalId))
    }
      .viaMat(KillSwitches.single)(Keep.right)
      .collect {
        case a: ProducerMessage.MultiResult[_, String, _] =>
          a.parts.map(a => a.record.value)

      }
      .toMat(Sink.seq[Seq[String]])(Keep.both)
      .run()

  }

  def transactionalId: String = java.util.UUID.randomUUID().toString
}
