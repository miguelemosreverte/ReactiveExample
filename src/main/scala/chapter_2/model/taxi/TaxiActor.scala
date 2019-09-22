package chapter_2.model.taxi

import akka.ShardedEntity
import akka.actor.{ActorLogging, ActorRef, Kill, Props, Terminated}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import akka.util.Timeout
import chapter_2.model.taxi.taxiDriver.TaxiDriver
import chapter_2.model.taxiDriver.ChangeDriver
import ddd.{Event, GetState}

import scala.concurrent.duration._
import akka.pattern.{ask, pipe}

class TaxiActor extends PersistentActor {
  import TaxiActor._
  private var state = TaxiState(Coordinate(0,0))
  implicit val ec = context.system.dispatcher
  implicit val timeout: Timeout = 1 second

  val taxiDriver: ActorRef = context.actorOf(TaxiDriver.props())

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

    case cmd:ChangeDriver =>
      taxiDriver ! cmd

    case GetDriver(aggregateRoot) =>
      (taxiDriver ? GetState(aggregateRoot)).pipeTo(sender())

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

