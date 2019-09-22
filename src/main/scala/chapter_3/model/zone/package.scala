package chapter_3.model

import java.time.LocalDateTime

import ddd.{Command, Event, Query, Response}

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
  case class PassengerArrives(aggregateRoot: String, deliveryId: BigInt, timestamp: LocalDateTime) extends Command

  case class PassengerLeaves(aggregateRoot: String, deliveryId: BigInt, timestamp: LocalDateTime) extends Command

  // Response
  case class ArriveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response

  case class LeaveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response


  // Queries
  //How many people are usually in the zone?
  case class HowManyPeopleAreUssuallyInTheZone(aggregateRoot: String) extends Query

  //How many people are usually in the zone around this hour?
  case class HowManyPeopleTodayAtThisHour(aggregateRoot: String) extends Query

  case class HowManyPeopleAreUsuallyTodayAtThisHour(aggregateRoot: String) extends Query

  //How many people are usually in the zone this day of week?
  case class HowManyPeopleToday(
                                 aggregateRoot: String,
                                 day: LocalDateTime = LocalDateTime.now) extends Query

  case class HowManyPeopleAreUsuallyToday(
                                           aggregateRoot: String,
                                           day: LocalDateTime) extends Query {
    def toDayOfWeek = day.getDayOfWeek
  }

  //How many people are usually in the zone this day of week around this hour?
  case class HowManyPeopleAreUsuallyTodayAtThisHourThisDayOfTheWeek(
                                                                     aggregateRoot: String,
                                                                     day: LocalDateTime) extends Query {
    def toDayOfWeek = day.getDayOfWeek
    def toHour = day.getHour
  }

}
