package chapter_2.model.taxi

import akka.RestartActorSupervisorFactory
import akka.actor.{ActorRef, Kill, Props}
import akka.pattern.ask
import akka.util.Timeout
import ddd.GetState
import org.scalatest.Assertion
import utils.Spec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TaxiActorSpec extends Spec {

  after {
    utils.leveldb.LevelDBCleanup.cleanLevelDB()
  }

  "Sending SetLocation command to TaxiActor" should {
    "be reflected in TaxiActor state" in {
      for {
        a <- unitTest(
          SetLocation("1",1, Coordinate(1, 1)), TaxiState(Coordinate(1, 1))
        )
      } yield a
    }
  }


  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val supervisor = new RestartActorSupervisorFactory
  val taxiActor: ActorRef = supervisor.create(TaxiActor.props(), "TaxiActor")

  type aggregateRoot = String

  def update(a: SetLocation): Future[SetLocationSuccess] =
    (taxiActor ? a).mapTo[SetLocationSuccess]


  def set(a: SetLocation): Future[TaxiState] = {
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
                command: SetLocation,
                expectedState: TaxiState
              ): Future[Assertion] =

    set(command)
      .map { response =>

        assert {
          response.location == expectedState.location
        }

      }

}
