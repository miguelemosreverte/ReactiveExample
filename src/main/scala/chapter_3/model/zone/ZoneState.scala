package chapter_3.model.zone

import java.time.{DayOfWeek, LocalDateTime}

import ddd.{AbstractState, Event}

case class ZoneState(
                      passengers: Long,
                      passengersPerDay: Map[DayOfWeek, Long],
                      passengersPerHourOfDay: Map[Int, Long],
                      passengersPerDayPerHourOfDay: Map[DayOfWeek, Map[Int, Long]],
                    ) extends AbstractState {


  def +(event: Event): ZoneState = {
    val value: Int = getValue(event)
    val date: LocalDateTime = getDate(event)
    copy(
      passengers = passengersM(date, value),
      passengersPerDay = passengersPerDayM(date, value),
      passengersPerHourOfDay = passengersPerHourOfDayM(date, value),
      passengersPerDayPerHourOfDay = passengersPerDayPerHourOfDayM(date, value),
    )
  }


  def passengersM(date: LocalDateTime, value: Int) = {
    passengers + value
  }

  def passengersPerDayM(date: LocalDateTime, value: Int) = {
    val dayOfWeek: DayOfWeek = date.getDayOfWeek
    passengersPerDay + (dayOfWeek -> passengersPerDay.getOrElse[Long](dayOfWeek, 0).+(value))
  }

  def passengersPerHourOfDayM(date: LocalDateTime, value: Int) = {
    val hour: Int = date.getHour
    passengersPerHourOfDay + (hour -> passengersPerHourOfDay.getOrElse[Long](hour, 0).+(value))
  }

  def passengersPerDayPerHourOfDayM(date: LocalDateTime, value: Int) = {
    val dayOfWeek: DayOfWeek = date.getDayOfWeek
    passengersPerDayPerHourOfDay + (dayOfWeek -> passengersPerHourOfDayM(date, value))
  }

  def getValue(event: Event) = event match {
    case _: Arrived => 1
    case _: Left => -1
  }

  def getDate(event: Event) = event match {
    case Arrived(timestamp) => timestamp
    case Left(timestamp) => timestamp
  }
}
