package chapter_2.model

import ddd.StringAggregateRoot.StringAggregateRoot.{Command, Query}
import ddd._

package object taxi {

  // Domain
  case class Coordinate(lat: Double, long: Double)

  // Events
  case class Located(location: Coordinate) extends Event {
    def name = "Located"
  }

  // Commands
  case class SetLocation(aggregateRoot: String, deliveryId: BigInt, location: Coordinate) extends Command

  // Response
  case class SetLocationSuccess(deliveryId: BigInt, location: Coordinate) extends Response

  // Queries
  case class GetDriver(aggregateRoot: String) extends Query


}
