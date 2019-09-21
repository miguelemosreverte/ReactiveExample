package chapter_2.model.taxi

import akka.ShardedEntity
import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import ddd.{Event, GetState}

class TaxiActor extends PersistentActor {
  import TaxiActor._
  private var state = TaxiState(Coordinate(0,0))

  context.actorOf()
  override def receiveCommand: Receive = {
    case SetLocation(aggregateRoot,deliveryId,location) =>
      val evt = Located(location)
      persist(evt) { e =>
        state += e
        val response = SetLocationSuccess(deliveryId, location)
        sender() ! response
      }
    case GetState(aggregateRoot) =>
      sender() ! state



  }

  override def persistenceId: String = typeName + "-" + self.path.name

  override def receiveRecover: Receive = {
    case evt: Event =>
      state += evt
    case SnapshotOffer(_, snapshot: TaxiState) =>
      state = snapshot
  }
}
object TaxiActor extends ShardedEntity {

  val typeName = "TaxiActor"

  def props(): Props = Props(new TaxiActor)

}

