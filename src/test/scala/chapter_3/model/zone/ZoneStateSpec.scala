package chapter_3.model.zone

import java.time.{DayOfWeek, Instant, LocalDateTime, ZoneOffset}

import chapter_2.model.taxi.{Located, TaxiState}
import ddd.Event
import org.scalatest.{MustMatchers, WordSpec}

object ZoneStateSpec extends MustMatchers {

  def unitTest(
                event: Event,
                expectedState: ZoneState
              ): scalaz.State[ZoneState, Unit] = scalaz.State { s =>

    val _s = s + event

    _s.passengers must be(expectedState.passengers)
    _s.passengersPerDay must be(expectedState.passengersPerDay)
    _s.passengersPerHourOfDay must be(expectedState.passengersPerHourOfDay)
    _s.passengersPerDayPerHourOfDay must be(expectedState.passengersPerDayPerHourOfDay)
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

  "Updating the same obligation for a 'sujetoID' multiple times" should {
    "keep the saldo of the last update" in {
      val result = for {
        s1 <- unitTest(
          Arrived(LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)), ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> 1),
            Map(0 -> 1),
            Map(DayOfWeek.THURSDAY -> Map(0 -> 1))
          )
        )
        _ <- unitTest(
          Arrived(LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)), ZoneState(
            passengers = 2,
            Map(DayOfWeek.THURSDAY -> 2),
            Map(0 -> 2),
            Map(DayOfWeek.THURSDAY -> Map(0 -> 2))
          )
        )

        _ <- unitTest(
          Left(LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)), ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> 1),
            Map(0 -> 1),
            Map(DayOfWeek.THURSDAY -> Map(0 -> 1))
          )
        )
      } yield s1
      result.run(seed)
    }
  }
}
