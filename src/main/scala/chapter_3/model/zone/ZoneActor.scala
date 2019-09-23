package chapter_4.model.zone

import java.time.LocalDateTime

import akka.ShardedEntity
import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import ddd.{Event}
import ddd.GeoAggregateRoot.GeoAggregateRoot.GetState

class ZoneActor extends PersistentActor {

  import ZoneActor._

  private var state = ZoneState(
    0,
    Map.empty,
    Map.empty,
    Map.empty
  )

  override def receiveCommand: Receive = {
    case PassengerArrivesZone(aggregateRoot, deliveryId, timestamp) =>
      val evt = Arrived(timestamp)
      persist(evt) { e =>
        state += e
        val response = ArrivalSuccess(deliveryId, timestamp)
        sender() ! response
      }
    case PassengerLeavesZone(aggregateRoot, deliveryId, timestamp) =>
      val evt = Left(timestamp)
      persist(evt) { e =>
        state += e
        val response = LeaveSuccess(deliveryId, timestamp)
        sender() ! response
      }

    case GetState(aggregateRoot) =>
      sender() ! state


    case a:HowManyPeopleAreUsuallyToday =>
      sender() ! state.perDay(a.toDayOfWeek).avg
    case a:HowManyPeopleAreUsuallyTodayAtThisHourThisDayOfTheWeek =>
      sender() ! state.perDayPerHourOfDay(a.toDayOfWeek)(a.toHour).avg

  }

  override def persistenceId: String = typeName + "-" + self.path.name

  override def receiveRecover: Receive = {
    case evt: Event =>
      state += evt
    case SnapshotOffer(_, snapshot: ZoneState) =>
      state = snapshot
  }
}

object ZoneActor extends ShardedEntity {

  val typeName = "ZoneActor"

  def props(): Props = Props(new ZoneActor)

}


