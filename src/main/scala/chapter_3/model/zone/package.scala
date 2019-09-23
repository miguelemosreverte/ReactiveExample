package chapter_4.model

import java.time.LocalDateTime

import ddd.GeoAggregateRoot.GeoAggregateRoot._
import ddd._
import geojson.GeoPoint

package object zone {

  // Domain
  case class Zone(id: String)

  // Events
  sealed trait ZoneEventsFamily

  case class Arrived(timestamp: LocalDateTime) extends Event with ZoneEventsFamily {
    def name = "Arrived"
  }

  case class Left(timestamp: LocalDateTime) extends Event with ZoneEventsFamily {
    def name = "Left"
  }

  sealed trait ZoneCommandFamily {
    def aggregateRoot: GeoPoint

    def deliveryId: BigInt

    def timestamp: LocalDateTime
  }

  // Commands
  case class PassengerArrivesZone(
                                   aggregateRoot: GeoPoint,
                                   deliveryId: BigInt,
                                   timestamp: LocalDateTime)
    extends Command with ZoneCommandFamily

  case class PassengerLeavesZone(
                                  aggregateRoot: GeoPoint,
                                  deliveryId: BigInt,
                                  timestamp: LocalDateTime)
    extends Command with ZoneCommandFamily

  // Response

  sealed trait ZoneResponseFamily {
    def deliveryId: BigInt

    def timestamp: LocalDateTime
  }

  case class ArrivalSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response with ZoneResponseFamily

  case class LeaveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response with ZoneResponseFamily


  // Queries

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
