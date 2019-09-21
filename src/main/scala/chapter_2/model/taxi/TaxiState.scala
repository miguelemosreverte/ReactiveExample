package chapter_2.model.taxi

import chapter_2.model.ddd.{AbstractState, Event}


case class TaxiState(location: Coordinate) extends AbstractState{

  def +(event: Event): TaxiState = event match {
    case Located(location) =>
      copy(location = location)
  }
}
