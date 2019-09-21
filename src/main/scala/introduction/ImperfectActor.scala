package introduction

import akka.actor.Actor

class ImperfectActor extends Actor {

  var acc: Int = 0

  def receive: Receive = {
    case msg if acc != 1 =>
      acc += 1
      sender() ! msg
    case _ =>
      acc += 1
  }
}
