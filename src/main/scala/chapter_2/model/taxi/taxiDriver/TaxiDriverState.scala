package chapter_2.model.taxi.taxiDriver

import chapter_2.model.taxiDriver.{ChangedDriver, Driver}
import ddd.{AbstractState, Event}


case class TaxiDriverState(driver: Driver) extends AbstractState{

  def +(event: Event): TaxiDriverState = event match {
    case ChangedDriver(driver) =>
      copy(driver = driver)
  }
}
