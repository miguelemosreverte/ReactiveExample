package chapter_2.model.taxi

import ddd.{AbstractState, Event}
import geojson.GeoPoint


case class TaxiState(
                      location: GeoPoint,
                      occupied: Boolean = false
                    ) extends AbstractState{

  def +(event: Event): TaxiState = event match {
    case TookPassenger(location) =>
      copy(
        location = location,
        occupied = true
      )
    case DroppedPassenger(location) =>
      copy(
        location = location,
        occupied = false
      )
  }
}
