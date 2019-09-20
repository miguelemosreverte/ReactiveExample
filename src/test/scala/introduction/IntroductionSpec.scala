package introduction

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import introduction.ImperfectActor
import utils.ClusterPCSSpec

import scala.concurrent.Future
import scala.concurrent.duration._

class IntroductionSpec extends ClusterPCSSpec {


  "ImperfectActor" should {
    " should reply half the times" in {


      for {
        a <- getReply("Hello there!")
        b <- getReply("Hello there!")

      } yield assert(a == "Hello there!" && b == "AskTimeoutException")
    }
  }

  def getReply(message: String): Future[String] =

    (imperfectActor ? message).mapTo[String].recoverWith {
      case a => Future(a.getClass.getSimpleName)
    }

  implicit val timeout: Timeout = Timeout(10 seconds)
  val imperfectActor: ActorRef = system.actorOf(Props(new ImperfectActor()), "ImperfectActor")


}
