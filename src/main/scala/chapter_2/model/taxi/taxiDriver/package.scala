package chapter_2.model

import chapter_2.model.taxi.Coordinate
import ddd._

package object taxiDriver {

  // Domain
  case class Driver(name: Option[String])

  // Events
  case class ChangedDriver(driver: Driver) extends Event {
    def name = "ChangedDriver"
  }

  // Commands
  case class ChangeDriver(aggregateRoot: String, deliveryId: BigInt, driver: Driver) extends Command



}
