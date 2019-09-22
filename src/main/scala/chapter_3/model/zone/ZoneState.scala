package chapter_3.model.zone

import java.time.temporal.ChronoUnit
import java.time.{DayOfWeek, LocalDateTime}

import ddd.{AbstractState, Event}

import chapter_3.model.zone.services.Queries._

case class ZoneState(
                      passengers: Long,
                      perDay: Map[DayOfWeek, AverageOf],
                      perHourOfDay: Map[Int, AverageOf],
                      perDayPerHourOfDay: Map[DayOfWeek, Map[Int, AverageOf]],
                    ) extends AbstractState {


  def +(event: Event): ZoneState = {
    val value: Int = getValue(event)
    val scoredValue: Int = getScoredValue(event)
    val date: LocalDateTime = getDate(event)
    implicit val a: ZoneState = this
    copy(
      passengers =         passengersM(date, value),
      perDay =             perDayM(date, scoredValue),
      perHourOfDay =       perHourOfDayM(date, scoredValue),
      perDayPerHourOfDay = perDayPerHourOfDayM(date, scoredValue),
    )
  }


}
