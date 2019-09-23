package chapter_4.model.trip

import akka.RestartActorSupervisorFactory
import akka.actor.{ActorRef, Kill}
import akka.pattern.ask
import akka.util.Timeout
import chapter_4.model.Trip.TripState
import ddd.GeoAggregateRoot.GeoAggregateRoot.{Command, GetState}
import geojson.GeoPoint
import org.scalatest.Assertion
import utils.Spec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TripActorSpec extends Spec {

  after {
    utils.leveldb.LevelDBCleanup.cleanLevelDB()
  }


  import utils.generators.RandomGeoPoints._

  val barrioNorte = inBarrioNorte
  val retiro = inRetiro

  "Sending Command command to TripActor" should {
    "be reflected in TripActor state" in {
      for {
        a <- unitTest(
          StartTrip(barrioNorte, 1), TripState(
            Trip(Some(barrioNorte), None)
          )
        )
      } yield a
    }
  }

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val supervisor = new RestartActorSupervisorFactory
  val tripActor: ActorRef = supervisor.create(TripActor.props(), "TaxiActor")


  def update(a: Command): Future[TripResponseFamily] =
    (tripActor ? a).mapTo[TripResponseFamily]


  def set(a: Command): Future[TripState] = {
    for {
      response <- update(a)
      _ <- kill
      state <- getState(a.aggregateRoot)
    } yield state
  }

  def getState(aggregateRoot: GeoPoint): Future[TripState] =
    (tripActor ? GetState(aggregateRoot)).mapTo[TripState]


  def kill: Future[Unit] = {
    tripActor ! Kill
    Future {
      Thread.sleep(1000)
    }
  }

  def unitTest(
                command: Command,
                expectedState: TripState
              ): Future[Assertion] =

    set(command)
      .map { response =>

        assert {
          response.trip == expectedState.trip
        }

      }

}
