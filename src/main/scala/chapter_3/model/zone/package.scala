package chapter_3.model

import java.time.LocalDateTime

import ddd.{Command, Event, Response}

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
  case class PassengerLeaves(aggregateRoot:  String, deliveryId: BigInt, timestamp: LocalDateTime) extends Command

  // Response
  case class ArriveSuccess(deliveryId: BigInt, timestamp: LocalDateTime) extends Response
  case class LeaveSuccess( deliveryId: BigInt, timestamp: LocalDateTime) extends Response


  // Queries
  // How many people are now in this zone?

  // How many people are in this zone per day?
}
