package chapter_2.model.taxi.taxiDriver

import akka.ShardedEntity
import akka.actor.{Actor, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import chapter_2.model.taxi.TaxiActor
import chapter_2.model.taxiDriver.{ChangeDriver, ChangedDriver, Driver}
import ddd.{Event, GetState}

class TaxiDriver extends Actor {
  import TaxiDriver._
  private var state = TaxiDriverState(Driver(None))

  override def receive: Receive = {
    case ChangeDriver(aggregateRoot,deliveryId,driver) =>
      val evt = ChangedDriver(driver)
      state += evt

    case GetState(aggregateRoot) =>
      sender() ! state





  }
}

object TaxiDriver extends ShardedEntity {

  val typeName = "TaxiDriver"

  def props(): Props = Props(new TaxiDriver)

}


