package chapter_2.model.taxi

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import chapter_2.model.ddd.Event

class TaxiActor extends PersistentActor {
  private var state = TaxiState(Coordinate(0,0))

  override def receiveCommand: Receive = {
    case SetLocation(location) =>
      val evt = Located(location)
      persist(evt) { e =>
        state += e
        val response = SetLocationSuccess(location)
        sender() ! response
      }


  }

  override def persistenceId: String = self.path.name

  override def receiveRecover: Receive = {
    case evt: Event =>
      state += evt
    case SnapshotOffer(_, snapshot: TaxiState) =>
      state = snapshot
  }
}
