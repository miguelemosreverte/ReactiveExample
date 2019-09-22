package chapter_3.model.zone

import java.time.{DayOfWeek, Instant, LocalDateTime, ZoneOffset}

import akka.RestartActorSupervisorFactory
import akka.actor.{ActorRef, Kill}
import akka.pattern.ask
import akka.util.Timeout
import ddd.{Command, GetState, Response}
import org.scalatest.Assertion
import utils.Spec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ZoneActorSpec extends Spec {

  after {
    utils.leveldb.LevelDBCleanup.cleanLevelDB()
  }


  "Passengers arrivals" should {
    "be reflected in ZoneActor state" in {
      for {
        a <- unitTest(
          PassengerArrives("1", 1, LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)),
          ZoneState(
            passengers = 1,
            Map(DayOfWeek.THURSDAY -> 1),
            Map(0 -> 1),
            Map(DayOfWeek.THURSDAY -> Map(0 -> 1))
          )
        )
        b <- unitTest(
          PassengerLeaves("1", 1, LocalDateTime.ofInstant(Instant.ofEpochMilli(7000), ZoneOffset.UTC)),
          ZoneState(
            passengers = 0,
            Map(DayOfWeek.THURSDAY -> 0),
            Map(0 -> 0),
            Map(DayOfWeek.THURSDAY -> Map(0 -> 0))
          )
        )
      } yield a
    }
  }

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val supervisor = new RestartActorSupervisorFactory
  val zoneActor: ActorRef = supervisor.create(ZoneActor.props(), "ZoneActor")

  type aggregateRoot = String

  def update(a: Command): Future[Response] =
    (zoneActor ? a).mapTo[Response]


  def set(a: Command): Future[ZoneState] = {
    for {
      _ <- update(a)
      _ <- kill
      state <- getState(a.aggregateRoot)
    } yield state
  }

  def getState(aggregateRoot: aggregateRoot = "1"): Future[ZoneState] =
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
            response.passengersPerDay == expectedState.passengersPerDay &&
            response.passengersPerHourOfDay == expectedState.passengersPerHourOfDay &&
            response.passengersPerDayPerHourOfDay == expectedState.passengersPerDayPerHourOfDay
        }

      }

}
