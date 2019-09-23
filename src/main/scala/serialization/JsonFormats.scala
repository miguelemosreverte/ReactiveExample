package serialization

import chapter_2.model.taxi.{DroppedPassenger, TookPassenger, _}
import chapter_4.model.trip.{EndTrip, EndTripSuccess, StartTrip, StartTripSuccess, TripCommandFamily, TripRequest, TripResponseFamily}
import chapter_4.model.zone.{ArrivalSuccess, LeaveSuccess, PassengerArrivesZone, PassengerLeavesZone, _}
import geojson.GeoPoint
import play.api.libs.json.Json
import serialization.json.{JsonSerializer, PlayJsonSerializer}

import scala.concurrent.Future

object JsonFormats {


  implicit val GeoPointF = Json.format[GeoPoint]

  object Taxi {

    implicit val TakePassengerF = Json.format[TakePassenger]
    implicit val DropPassengerF = Json.format[DropPassenger]
    implicit val TakePassengerSuccessF = Json.format[TakePassengerSuccess]
    implicit val DropPassengerSuccessF = Json.format[DropPassengerSuccess]
    implicit val TookPassengerF = Json.format[TookPassenger]
    implicit val DroppedPassenger = Json.format[DroppedPassenger]

    /*implicit object TripProposalF extends PlayJsonSerializer[TripProposal] {
      implicit override def oformat = Json.format[TripProposal]
    }*/

    implicit object TakeOrDropPassengerF extends PlayJsonSerializer[TaxiCommandFamily] {
      implicit override def oformat = Json.format[TaxiCommandFamily]
    }

    implicit object TakeOrDropPassengerSuccessF extends PlayJsonSerializer[TaxiResponseFamily] {
      implicit override def oformat = Json.format[TaxiResponseFamily]
    }

  }

  object Zone {
    implicit val PassengerArrivesZoneF = Json.format[PassengerArrivesZone]
    implicit val PassengerLeavesZoneF = Json.format[PassengerLeavesZone]
    implicit val ArrivalSuccessF = Json.format[ArrivalSuccess]
    implicit val LeaveSuccessF = Json.format[LeaveSuccess]

    implicit object PassengerArrivesOrLeavesZoneF extends PlayJsonSerializer[ZoneCommandFamily] {
      implicit override def oformat = Json.format[ZoneCommandFamily]
    }

    implicit object PassengerArrivesOrLeavesZoneSuccessF extends PlayJsonSerializer[ZoneResponseFamily] {
      implicit override def oformat = Json.format[ZoneResponseFamily]
    }

  }


  object Trip {
    implicit val StartTripF = Json.format[StartTrip]
    implicit val EndTripF = Json.format[EndTrip]
    implicit val StartTripSuccessF = Json.format[StartTripSuccess]
    implicit val EndTripSuccessF = Json.format[EndTripSuccess]

    implicit val tripRequestSerializer: JsonSerializer[TripRequest] = TripRequestF
    implicit object TripRequestF extends PlayJsonSerializer[TripRequest] {
      implicit override def oformat = Json.format[TripRequest]
    }

    implicit object TripCommandFamilyF extends PlayJsonSerializer[TripCommandFamily] {
      implicit override def oformat = Json.format[TripCommandFamily]
    }

    implicit object TripResponseFamilyF extends PlayJsonSerializer[TripResponseFamily] {
      implicit override def oformat = Json.format[TripResponseFamily]
    }

  }


  object MessageSerializer {

    case class Message(value: String = "Done")

    class MessageEventSerializer extends EventSerializer[Message]

    implicit val serializer: JsonSerializer[Message] = Serializer

    implicit object Serializer extends PlayJsonSerializer[Message] {
      implicit override def oformat = Json.format[Message]
    }


    object MessageSerializerTypeClass {


      trait Trait[A] {
        implicit def encode(a: A): String

        implicit def decode(a: String): Either[String, A]
      }

      def apply[A](implicit sh: Trait[A]): Trait[A] = sh

      def encode[A: Trait](a: A) = MessageSerializerTypeClass[A].encode(a)

      def decode[A: Trait](a: String) = MessageSerializerTypeClass[A].decode(a)

      implicit class EncodeOps[A: Trait](a: Message) {
        def encode = MessageSerializerTypeClass[Message].encode(a)
      }

      implicit class DecodeOps[A: Trait](a: String) {
        def decode = MessageSerializerTypeClass[A].decode(a)
      }

      implicit val implicitSerializer: Trait[Message] =
        new Trait[Message] {
          def encode(value: Message): String = serializer.encode(value)

          def decode(value: String): Either[String, Message] = serializer.decode(value)
        }

      Message("Example message").encode.decode

    }

  }

  object Transaction {
    object EmptyResponseSerializer {

      case class EmptyResponse(value: String = "Done")

      implicit val EmptyResponseF = Json.format[EmptyResponse]

      implicit object EmptyResponseFF extends PlayJsonSerializer[EmptyResponse] {
        implicit override def oformat = EmptyResponseF
      }

      implicit def toFutureSeq(a: EmptyResponse): Future[Seq[EmptyResponse]] =
        Future.successful(Seq(a))
    }
  }



}
