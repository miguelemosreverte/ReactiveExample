package chapter_2.model.taxi.taxiDriver

import akka.ShardedEntity
import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import chapter_2.model.taxi.TaxiActor
import chapter_2.model.taxiDriver.{ChangeDriver, ChangedDriver, Driver}
import ddd.{Event, GetState}

class TaxiDriver extends PersistentActor {
  import TaxiDriver._
  private var state = TaxiDriverState(Driver(None))

  override def receiveCommand: Receive = {
    case ChangeDriver(aggregateRoot,deliveryId,driver) =>
      val evt = ChangedDriver(driver)
      persist(evt) { e =>
        state += e
      }
    case GetState(aggregateRoot) =>
      sender() ! state



  }

  override def persistenceId: String = typeName + "-" + self.path.name

  override def receiveRecover: Receive = {
    case evt: Event =>
      state += evt
    case SnapshotOffer(_, snapshot: TaxiDriverState) =>
      state = snapshot
  }
}

object TaxiDriver extends ShardedEntity {

  val typeName = "TaxiDriver"

  def props(): Props = Props(new TaxiDriver)

}


