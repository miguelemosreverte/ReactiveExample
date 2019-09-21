package chapter_2.model.taxi

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
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
          SetLocation(Coordinate(1, 1)), SetLocationSuccess(Coordinate(1, 1))
        )
      } yield a
    }
  }


  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val taxiActor: ActorRef = system.actorOf(Props(new TaxiActor()), "TaxiActor")

  type aggregateRoot = String

  def update(a: SetLocation): Future[SetLocationSuccess] =
    (taxiActor ? a).mapTo[SetLocationSuccess]


  def set(a: SetLocation): Future[SetLocationSuccess] = {
    for {
      response <- update(a)
    } yield response
  }

  def unitTest(
                command: SetLocation,
                expectedResponse: SetLocationSuccess
              ): Future[Assertion] =

    set(command)
      .map { response =>

        assert {
          response == expectedResponse
        }

      }

}
