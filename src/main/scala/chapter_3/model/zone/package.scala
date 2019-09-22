package chapter_3.model

import java.time.LocalDateTime

import ddd._
import ddd.GeoAggregateRoot.GeoAggregateRoot._
import geojson.GeoPoint

package object zone {

  // Domain
  case class Zone(id: String)

  // Events
  case class Arrived(timestamp: LocalDateTime) extends Event {
    def name = "Arrived"
  }

  case class Left(timestamp: LocalDateTime) extends Event {
    def name = "Left"
  }

  // Commands
  case class PassengerArrives(
                               aggregateRoot: GeoPoint,
                               deliveryId: BigInt,
                               timestamp: LocalDateTime)
    extends Command

  case class PassengerLeaves(
                              aggregateRoot: GeoPoint,
                              deliveryId: BigInt,
                              timestamp: LocalDateTime)
    extends Command

  // Response
  case class ArriveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response

  case class LeaveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response


  // Queries
  case class GetState(aggregateRoot: GeoPoint) extends Query

  //How many people are usually in the zone?
  case class HowManyPeopleAreUssuallyInTheZone(aggregateRoot: GeoPoint) extends Query

  //How many people are usually in the zone around this hour?
  case class HowManyPeopleTodayAtThisHour(aggregateRoot: GeoPoint) extends Query

  case class HowManyPeopleAreUsuallyTodayAtThisHour(aggregateRoot: GeoPoint) extends Query

  //How many people are usually in the zone this day of week?
  case class HowManyPeopleToday(
                                 aggregateRoot: GeoPoint,
                                 day: LocalDateTime = LocalDateTime.now) extends Query

  case class HowManyPeopleAreUsuallyToday(
                                           aggregateRoot: GeoPoint,
                                           day: LocalDateTime) extends Query {
    def toDayOfWeek = day.getDayOfWeek
  }

  //How many people are usually in the zone this day of week around this hour?
  case class HowManyPeopleAreUsuallyTodayAtThisHourThisDayOfTheWeek(
                                                                     aggregateRoot: GeoPoint,
                                                                     day: LocalDateTime) extends Query {
    def toDayOfWeek = day.getDayOfWeek
    def toHour = day.getHour
  }

}
