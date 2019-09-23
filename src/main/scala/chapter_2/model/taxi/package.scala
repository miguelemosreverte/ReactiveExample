package chapter_2.model

import ddd.StringAggregateRoot.StringAggregateRoot.{Command, Query}
import ddd._
import geojson.GeoPoint

package object taxi {

  // Domain

  // Events
  sealed trait TaxiEventFamily

  case class TookPassenger(location: GeoPoint) extends Event with TaxiEventFamily{
    def name = "TookPassenger"
  }

  case class DroppedPassenger(location: GeoPoint) extends Event with TaxiEventFamily{
    def name = "DroppedPassenger"
  }

  // Commands
  sealed trait TaxiCommandFamily

  case class TakePassenger(aggregateRoot: String, deliveryId: BigInt, location: GeoPoint) extends Command with TaxiCommandFamily

  case class DropPassenger(aggregateRoot: String, deliveryId: BigInt, location: GeoPoint) extends Command with TaxiCommandFamily

  // Response
  sealed trait TaxiResponseFamily {
    def deliveryId: BigInt

    def location: GeoPoint
  }

  case class TakePassengerSuccess(deliveryId: BigInt, location: GeoPoint) extends Response with TaxiResponseFamily

  case class DropPassengerSuccess(deliveryId: BigInt, location: GeoPoint) extends Response with TaxiResponseFamily

  // Queries
  case class GetDriver(aggregateRoot: String) extends Query


}
