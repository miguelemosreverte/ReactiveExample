package introduction

import akka.actor.Actor

class ImperfectActor extends Actor {

  var acc: Int = 0

  def receive: Receive = {
    case msg if acc % 2 == 0 =>
      acc += 1
      sender() ! msg
  }
}
