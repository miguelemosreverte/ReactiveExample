package chapter_3.model.zone

import java.time.LocalDateTime

import akka.ShardedEntity
import akka.actor.Props
import akka.persistence.{PersistentActor, SnapshotOffer}
import ddd.{Event, GetState}

class ZoneActor extends PersistentActor {

  import ZoneActor._

  private var state = ZoneState(
    0,
    Map.empty,
    Map.empty,
    Map.empty
  )

  override def receiveCommand: Receive = {
    case PassengerArrives(aggregateRoot, deliveryId, timestamp) =>
      val evt = Arrived(timestamp)
      persist(evt) { e =>
        state += e
        val response = ArriveSuccess(deliveryId, timestamp)
        sender() ! response
      }
    case PassengerLeaves(aggregateRoot, deliveryId, timestamp) =>
      val evt = Left(timestamp)
      persist(evt) { e =>
        state += e
        val response = LeaveSuccess(deliveryId, timestamp)
        sender() ! response
      }

    case GetState(aggregateRoot) =>
      sender() ! state

      /*

  case class HowManyPeopleAreUssuallyInTheZone(aggregateRoot: String) extends Query

  //How many people are usually in the zone around this hour?
  case class HowManyPeopleTodayAtThisHour(aggregateRoot: String) extends Query
  case class HowManyPeopleAreUsuallyTodayAtThisHour(aggregateRoot: String) extends Query

  //How many people are usually in the zone this day of week?
  case class HowManyPeopleToday(aggregateRoot: String) extends Query
  case class HowManyPeopleAreUsuallyToday(aggregateRoot: String) extends Query

  //How many people are usually in the zone this day of week around this hour?
  case class HowManyPeopleAreUsuallyTodayAtThisHourThisDayOfTheWeek(aggregateRoot: String) extends Query
       */
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


