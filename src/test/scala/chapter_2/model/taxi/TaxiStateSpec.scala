package chapter_2.model.taxi

import ddd.Event
import geojson.GeoPoint
import org.scalatest.{MustMatchers, WordSpec}

object TaxiStateSpec extends MustMatchers {

  def unitTest(
                event: Event,
                expectedState: TaxiState
              ): scalaz.State[TaxiState, Unit] = scalaz.State { s =>

    val _s = s + event

    _s.location must be(expectedState.location)
    (_s, ())
  }

}

class TaxiStateSpec extends WordSpec with MustMatchers {

  import TaxiStateSpec._

  val seed = TaxiState(GeoPoint(0,0))

  "Updating TaxiState" should {
    "reflect changes" in {
      val result = for {
        s1 <- unitTest(
          TookPassenger(GeoPoint(1, 1)), TaxiState(GeoPoint(1, 1))
        )
      } yield s1
      result.run(seed)
    }
  }
}
