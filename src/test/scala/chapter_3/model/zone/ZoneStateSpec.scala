package chapter_4.model.zone

import java.time.temporal.ChronoUnit
import java.time.{DayOfWeek, Instant, LocalDateTime, ZoneOffset}

import chapter_4.model.zone.services.Queries.AverageOf
import ddd.Event
import org.scalatest.{MustMatchers, WordSpec}

object ZoneStateSpec extends MustMatchers {

  def unitTest(
                event: Event,
                expectedState: ZoneState
              ): scalaz.State[ZoneState, Unit] = scalaz.State { s =>

    val _s = s + event

    _s.passengers must be(expectedState.passengers)
    _s.perDay must be(expectedState.perDay)
    _s.perHourOfDay must be(expectedState.perHourOfDay)
    _s.perDayPerHourOfDay must be(expectedState.perDayPerHourOfDay)
    (_s, ())
  }

}

class ZoneStateSpec extends WordSpec with MustMatchers {

  import ZoneStateSpec._

  val seed = ZoneState(
    0,
    Map.empty,
    Map.empty,
    Map.empty
  )
  val givenDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)
  val perDay = givenDate.truncatedTo(ChronoUnit.DAYS)
  val perHour = givenDate.truncatedTo(ChronoUnit.HOURS)

  val nextThursday = givenDate.plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS)

  "Updating ZoneState" should {
    "work as intended" in {
      val result = for {
        s1 <- unitTest(
          Arrived(givenDate), ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> AverageOf(1, Set(perDay))),
            Map(0 -> AverageOf(1, Set(perHour))),
            Map(DayOfWeek.THURSDAY -> Map(0 -> AverageOf(1, Set(perHour))))
          )
        )
        _ <- unitTest(
          Arrived(givenDate), ZoneState(
            passengers = 2,
            Map(DayOfWeek.THURSDAY -> AverageOf(2, Set(perDay))),
            Map(0 -> AverageOf(2, Set(perHour))),
            Map(DayOfWeek.THURSDAY -> Map(0 -> AverageOf(2, Set(perHour))))
          )
        )

        _ <- unitTest(
          Left(nextThursday), ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> AverageOf(2, Set(perDay, nextThursday))),
            Map(0 -> AverageOf(2, Set(perHour, nextThursday))),
            Map(DayOfWeek.THURSDAY -> Map(0 -> AverageOf(2, Set(perHour, nextThursday))))
          )

        )
      } yield s1
      result.run(seed)
    }
  }
}
