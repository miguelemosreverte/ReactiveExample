package chapter_2.model.taxi

import akka.RestartActorSupervisorFactory
import akka.actor.{ActorRef, Kill}
import akka.pattern.ask
import akka.util.Timeout
import chapter_2.model.taxi.taxiDriver.TaxiDriverState
import chapter_2.model.taxiDriver.{ChangeDriver, Driver}
import ddd.StringAggregateRoot.StringAggregateRoot.{Command, GetState}
import geojson.GeoPoint
import org.scalatest.Assertion
import utils.Spec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TaxiActorSpec extends Spec {

  after {
    utils.leveldb.LevelDBCleanup.cleanLevelDB()
  }

  def toBigInt(i: Int): BigInt = BigInt(i)


  "Sending SetLocation command to TaxiActor" should {
    "be reflected in TaxiActor state" in {
      for {
        a <- unitTest(
          TakePassenger("1", 1, GeoPoint(1, 1)), TaxiState(GeoPoint(1, 1))
        )
      } yield a
    }
  }
  "Sending ChangeDriver to TaxiActor" should {
    "change driver" in {
      for {
        before <- (taxiActor ? GetDriver("1")).mapTo[TaxiDriverState]
        _ =   taxiActor ! ChangeDriver("1", toBigInt(1), Driver(Some("1")))
        after <- (taxiActor ? GetDriver("1")).mapTo[TaxiDriverState]

      } yield assert(
        before.driver == Driver(None)
        && after.driver == Driver(Some("1"))
      )
    }
  }

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val supervisor = new RestartActorSupervisorFactory
  val taxiActor: ActorRef = supervisor.create(TaxiActor.props(), "TaxiActor")

  type aggregateRoot = String

  def update(a: Command): Future[TakePassengerSuccess] =
    (taxiActor ? a).mapTo[TakePassengerSuccess]


  def set(a: Command): Future[TaxiState] = {
    for {
      response <- update(a)
      _ <- kill
      state <- getState(a.aggregateRoot)
    } yield state
  }

  def getState(aggregateRoot: aggregateRoot = "1"): Future[TaxiState] =
    (taxiActor ? GetState(aggregateRoot)).mapTo[TaxiState]


  def kill: Future[Unit] = {
    taxiActor ! Kill
    Future {
      Thread.sleep(1000)
    }
  }

  def unitTest(
                command: Command,
                expectedState: TaxiState
              ): Future[Assertion] =

    set(command)
      .map { response =>

        assert {
          response.location == expectedState.location
        }

      }

}
