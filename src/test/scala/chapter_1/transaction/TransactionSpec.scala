package chapter_1.transaction


import akka.Done
import akka.actor.{ActorRef, Props}
import akka.kafka.scaladsl.Producer
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import introduction.ImperfectActor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import transaction.Transaction.□
import utils.DocsSpecBase

import scala.concurrent.Future

class TransactionSpec extends DocsSpecBase(9095) {

  val requests: Seq[String] = Seq(
    "hello there",
    "hello again"
  )
  import serialization.JsonFormats.MessageSerializer.MessageSerializerTypeClass._
  import serialization.JsonFormats.MessageSerializer._

  lazy val results = executeTransaction(
    requests
       map {
        Message
      }
       map {
        _.encode
      }
  )

  "All requests" should "be echoed by the Imperfect Actor, thanks to the retry effort made by Transaction" in {

    for {
      r <- results
      _ = println(r)
    } yield assert(requests.forall(request => r.toString.contains(request)))

  }

  def executeTransaction(requests: Seq[String]) = {

    implicit def toSeqF(a: Future[Message]): Future[Seq[Message]] = a.map(aa => Seq(aa))

    import scala.concurrent.duration._
    implicit val timeout: Timeout = Timeout(1.seconds)
    def getReply(message: Message): Future[Seq[Message]] =
      (imperfectActor ? message).mapTo[Message]

    val flow = □[Message, Message](sourceTopic, sinkTopic, getReply).run()
    val control = flow._1
    val results = flow._2

    def produce(topic: String, data: Seq[String]): Future[Done] =
      Source(data.to[collection.immutable.Seq])
        .map(n =>
          new ProducerRecord[String, String](sourceTopic, n))
        .runWith(Producer.plainSink(producer))


    produce(sourceTopic, requests) flatMap {
      case Done =>
        for {
          _ <- Future(
            Thread.sleep(40000L)
          ) map { _ =>
              control.shutdown()
            }
          result <- results

        } yield result

    }

  }

  val imperfectActor: ActorRef = system.actorOf(Props(new ImperfectActor()), "ImperfectActorTransactionalTest")

  val sourceTopic = "sourceTopic"
  val sinkTopic = "sinkTopic"

  implicit val consumer: ConsumerSettings[String, String] = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  implicit val producer: ProducerSettings[String, String] = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)


  implicit override val ec = system.dispatcher

}

