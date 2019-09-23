package chapter_3.transactionFlow

package transactionFlow

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.pattern.ask
import akka.util.Timeout
import chapter_1.transaction.Transaction._
import chapter_2.model.taxi._
import chapter_4.model.zone._

import scala.concurrent.{ExecutionContext, Future}

object TransactionFlow {

  def executeTransactions(initialTopic: String)(implicit system: ActorSystem, t: Timeout, ec: ExecutionContext,
                                                consumer: ConsumerSettings[String, String],
                                                producer: ProducerSettings[String, String]) = {


    implicit def toFutureSeq[A](a: Future[A]): Future[Seq[A]] = a.map(Seq(_))


    val taxiActor = TaxiActor.start
    val zoneActor = ZoneActor.start


    def updateTaxi(a: A1): Future[A3] =
      for {
        a <- (taxiActor ? a).mapTo[TaxiResponseFamily]
      } yield a

    def updateZone(a: A3): Future[A4] =
      for {
        a <- (zoneActor ? a).mapTo[ZoneResponseFamily]
      } yield a

    import serialization.JsonFormats.Taxi._
    import serialization.JsonFormats.Zone._
    type A1 = TaxiCommandFamily
    type A3 = ZoneCommandFamily
    type A4 = ZoneResponseFamily


    □[A1, A3](initialTopic, "TaxiEvent", {
      updateTaxi
    })

    □[A3, A4]("TaxiEvent", "ZoneEvent", {
      updateZone
    })
  }

  implicit def taxiResponseToZoneCommand(response: TaxiResponseFamily): ZoneCommandFamily =
    response match {
      case TakePassengerSuccess(deliveryId, location) =>
        PassengerLeavesZone(location, deliveryId, LocalDateTime.now)
      case DropPassengerSuccess(deliveryId, location) =>
        PassengerArrivesZone(location, deliveryId, LocalDateTime.now)
    }


}

