package chapter_2.model

import chapter_2.model.ddd._

package object taxi {

  // Domain
  case class Coordinate(lat: Double, long: Double)

  // Events
  case class Located(location: Coordinate) extends Event

  // Commands
  case class SetLocation(location: Coordinate) extends Command

  // Response
  case class SetLocationSuccess(location: Coordinate) extends Command


}
