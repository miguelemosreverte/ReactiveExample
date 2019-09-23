package chapter_4.model.zone

import java.time.temporal.ChronoUnit
import java.time.{DayOfWeek, Instant, LocalDateTime, ZoneOffset}

import akka.RestartActorSupervisorFactory
import akka.actor.{ActorRef, Kill}
import akka.pattern.ask
import akka.util.Timeout
import chapter_4.model.zone.services.Queries.AverageOf
import ddd.{Response}
import ddd.GeoAggregateRoot.GeoAggregateRoot._
import geojson.GeoPoint
import org.scalatest.Assertion
import utils.Spec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ZoneActorSpec extends Spec {

  after {
    utils.leveldb.LevelDBCleanup.cleanLevelDB()
  }

  val givenDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)
  val perDay = givenDate.truncatedTo(ChronoUnit.DAYS)
  val perHour = givenDate.truncatedTo(ChronoUnit.HOURS)

  val nextThursday = givenDate.plus(7, ChronoUnit.DAYS)

  val deliveryId = 1


  import utils.generators.RandomGeoPoints._

  "Passengers arrivals" should {
    "be reflected in ZoneActor state" in {
      for {
        a <- unitTest(
          PassengerArrivesZone(inRecoleta, deliveryId, givenDate),
          ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> AverageOf(1, Set(perDay))),
            Map(0 -> AverageOf(1, Set(perHour))),
            Map(DayOfWeek.THURSDAY -> Map(0 -> AverageOf(1, Set(perHour))))
          )
        )
        b <- unitTest(
          PassengerLeavesZone(inRecoleta, deliveryId, givenDate),
          ZoneState(
            passengers = 0,
            Map(DayOfWeek.THURSDAY -> AverageOf(1, Set(perDay))),
            Map(0 -> AverageOf(1, Set(perHour))),
            Map(DayOfWeek.THURSDAY -> Map(0 -> AverageOf(1, Set(perHour))))
          )
        )

        _ <- update(PassengerArrivesZone(inRecoleta, deliveryId, givenDate))
        _ <- update(PassengerArrivesZone(inRecoleta, deliveryId, givenDate))
        _ <- update(PassengerArrivesZone(inRecoleta, deliveryId, givenDate))
        _ <- update(PassengerArrivesZone(inRecoleta, deliveryId, nextThursday))

        state <- getState()
        actorSays <- zoneActor ? HowManyPeopleAreUsuallyToday(inRecoleta, givenDate)
        actorSaysWithMoreSpecificity <- zoneActor ? HowManyPeopleAreUsuallyTodayAtThisHourThisDayOfTheWeek(inRecoleta, givenDate)

      } yield assert(
        state.perDay(perDay.getDayOfWeek).avg == 2.5 
          && actorSays == 2.5
          && actorSaysWithMoreSpecificity == 2.5
      )
    }
  }

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val zoneActor: ActorRef = ZoneActor.start


  def update(a: Command): Future[Response] =
    (zoneActor ? a).mapTo[Response]


  def set(a: Command): Future[ZoneState] = {
    for {
      _ <- update(a)
      state <- getState(a.aggregateRoot)
    } yield state
  }

  def getState(aggregateRoot: GeoPoint = inRecoleta): Future[ZoneState] =
    (zoneActor ? GetState(aggregateRoot)).mapTo[ZoneState]


  def kill: Future[Unit] = {
    zoneActor ! Kill
    Future {
      Thread.sleep(1000)
    }
  }

  def unitTest(
                command: Command,
                expectedState: ZoneState
              ): Future[Assertion] =

    set(command)
      .map { response =>

        assert {
          response.passengers == expectedState.passengers &&
            response.perDay == expectedState.perDay &&
          response.perHourOfDay == expectedState.perHourOfDay &&
            response.perDayPerHourOfDay == expectedState.perDayPerHourOfDay
        }

      }

}
