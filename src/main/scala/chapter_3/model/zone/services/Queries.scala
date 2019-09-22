package chapter_3.model.zone.services

import java.time.temporal.ChronoUnit
import java.time.{DayOfWeek, LocalDateTime}

import chapter_3.model.zone.{Arrived, Left, ZoneState}
import ddd.Event

object Queries {

  case class AverageOf(sum: Long, n: Set[LocalDateTime]) {
    def avg: Double = sum.toDouble / n.size
  }

  def passengersM(date: LocalDateTime, value: Int)(implicit state: ZoneState) = {
    state.passengers + value
  }

  def perDayM(date: LocalDateTime, value: Int)(implicit state: ZoneState) = {
    val dayOfWeek: DayOfWeek = date.getDayOfWeek
    val current: AverageOf = state.perDay.getOrElse[AverageOf](dayOfWeek, AverageOf(0, Set.empty))
    val mod = current.copy(
      sum = current.sum + value,
      n = current.n + date.truncatedTo(ChronoUnit.DAYS)
    )
    state.perDay + (dayOfWeek -> mod)
  }

  def perHourOfDayM(date: LocalDateTime, value: Int)(implicit state: ZoneState) = {
    val hour: Int = date.getHour
    val current: AverageOf = state.perHourOfDay.getOrElse[AverageOf](hour, AverageOf(0, Set.empty))
    val mod = current.copy(
      sum = current.sum + value,
      n = current.n + date.truncatedTo(ChronoUnit.HOURS)
    )
    state.perHourOfDay + (hour -> mod)
  }

  def perDayPerHourOfDayM(date: LocalDateTime, value: Int)(implicit state: ZoneState) = {
    val dayOfWeek: DayOfWeek = date.getDayOfWeek
    state.perDayPerHourOfDay + (dayOfWeek -> perHourOfDayM(date, value))
  }

  def getValue(event: Event) = event match {
    case _: Arrived => 1
    case _: Left => -1
  }

  def getScoredValue(event: Event) = event match {
    case _: Arrived => 1
    case _: Left => 0
  }

  def getDate(event: Event) = event match {
    case Arrived(timestamp) => timestamp
    case Left(timestamp) => timestamp
  }
}
