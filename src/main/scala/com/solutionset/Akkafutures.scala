package com.solutionset

import akka.actor._
import akka.pattern.ask
import akka.util.duration._
import akka.util.Timeout

case object Tick
case object Get

class Counter extends Actor {
  var count = 0

  def receive = {
    case Tick => count += 1
    case Get  => Thread.sleep(5);sender ! count
  }
}

object Akkafutures extends App {
  val system = ActorSystem("Akkafutures")

  val counter = system.actorOf(Props[Counter])

  counter ! Tick
  counter ! Tick
  counter ! Tick

  implicit val timeout = Timeout(10 seconds)

  (counter ? Get) onSuccess {
    case count => println("Count is " + count)
  }

  println("Im done here!")

  system.shutdown()
}
