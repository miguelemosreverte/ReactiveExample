package chapter_2.model.taxi

import org.scalatest.{MustMatchers, WordSpec}

object TaxiStateSpec extends MustMatchers {

  def unitTest(
                event:                         Located,
                expectedState: TaxiState
              ): scalaz.State[TaxiState, Unit] = scalaz.State { s =>

    val _s = s + event

    _s.location must be (expectedState.location)
    (_s, ())
  }

}

class TaxiStateSpec extends WordSpec with MustMatchers {

  import TaxiStateSpec._

  val seed = TaxiState(Coordinate(0,0))

  "Sending Located event to TaxiState" should {
    "be reflected in TaxiState state" in {
      val result = for {
        s1 <- unitTest(
          Located(Coordinate(1,1)), TaxiState(Coordinate(1,1))
          )
      } yield s1
      result.run(seed)
    }
  }
}
