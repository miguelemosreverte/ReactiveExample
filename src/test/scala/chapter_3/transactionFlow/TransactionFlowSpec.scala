package chapter_3.transactionFlow

import akka.Done
import akka.kafka.scaladsl.Producer
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.stream.UniqueKillSwitch
import akka.stream.scaladsl.Source
import chapter_2.model.taxi.{DropPassenger, TakePassenger, TaxiCommandFamily}
import chapter_4.model.zone.{ZoneActor, ZoneState}
import ddd.GeoAggregateRoot.GeoAggregateRoot.GetState
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import serialization.json.JsonSerializer
import utils.DocsSpecBase


import scala.collection.immutable
import scala.concurrent.Future

class TransactionFlowSpec extends DocsSpecBase(9098) {

  import chapter_3.transactionFlow.transactionFlow.TransactionFlow.executeTransactions


  implicit val consumer: ConsumerSettings[String, String] = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  implicit val producer: ProducerSettings[String, String] = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  "All requests" should "be Transaction" in {

    implicit val ec = system.dispatcher
    val a: (UniqueKillSwitch, Future[immutable.Seq[Seq[String]]]) = executeTransactions(sourceTopic)
    val killSwitch = a._1
    val results = a._2

    def produce(topic: String, data: Seq[String]): Future[Done] =
      Source(data.to[collection.immutable.Seq])
        .map(n =>
          new ProducerRecord[String, String](sourceTopic, n))
        .runWith(Producer.plainSink(producer))

    val actorRef = ZoneActor.start
    produce(sourceTopic, requests) flatMap {
      case Done =>
        for {
          _ <- Future(
            Thread.sleep(30000L)
          ) map { _ =>
            killSwitch.shutdown()
          }
          result <- results
        } yield assert(result.size == requests.size)

    }
  }


  import utils.generators.RandomGeoPoints._

  val serializer: JsonSerializer[TaxiCommandFamily] = serialization.JsonFormats.Taxi.TakeOrDropPassengerF
  val sourceTopic = "SetTaxiLocation"
  val requests: Seq[String] = Seq(
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inBarrioNorte),
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inRetiro),
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inBarrioNorte),
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inRetiro),
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inBarrioNorte),
    TakePassenger("1", 1, inRecoleta),
    DropPassenger("1", 1, inBarrioNorte)
  ).map {
    case a: TaxiCommandFamily =>
      serializer.encode(a)
  }

}
