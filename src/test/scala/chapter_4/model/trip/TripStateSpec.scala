package chapter_4.model.Trip

package chapter_2.model.taxi

import chapter_4.model.trip.{EndedTrip, StartedTrip, Trip}
import ddd.Event
import geojson.GeoPoint
import org.scalatest.{MustMatchers, WordSpec}

object TripStateSpec extends MustMatchers {

  def unitTest(
                event: Event,
                expectedState: TripState
              ): scalaz.State[TripState, Unit] = scalaz.State { s =>

    val _s = s + event

    _s.trip must be(expectedState.trip)
    (_s, ())
  }

}

class TripStateSpec extends WordSpec with MustMatchers {

  import TripStateSpec._

  val seed = TripState(Trip(None, None))

  import utils.generators.RandomGeoPoints._
  val barrioNorte = inBarrioNorte
  val retiro = inRetiro
  "Updating TripState" should {
    "avoid malformed states" in {
      val result = for {
        s1 <- unitTest(
          EndedTrip(barrioNorte), TripState(
            Trip(None, None)
          ),
        )
        _ <- unitTest(
          StartedTrip(barrioNorte), TripState(
            Trip(Some(barrioNorte), None)
          )
        )
        _ <- unitTest(
          StartedTrip(barrioNorte), TripState(
            Trip(Some(barrioNorte), None)
          )
        )
        _ <- unitTest(
          EndedTrip(retiro), TripState(
            Trip(Some(barrioNorte), Some(retiro))

          )
        )

      } yield s1
      result.run(seed)
    }
  }
}
