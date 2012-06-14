package com.solutionset

import akka.dispatch.{Future, Await}
import akka.util.duration._

import akka.actor.{ActorSystem, Props, Actor}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import akka.util.Timeout._
import akka.util.duration._
import scala.collection.immutable.Range.Long._
import collection.immutable.NumericRange
import akka.routing.{DefaultResizer, RoundRobinRouter}
import com.typesafe.config.ConfigFactory


/**
 * Created with IntelliJ IDEA.
 * User: ripple.khera
 * Date: 6/13/12
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */

object FuturesTimeout extends App {

  implicit val timeout = Timeout(10 seconds)

  val customConf = ConfigFactory.parseString("""
      balancing-dispatcher {
        executor = "thread-pool-executor"
        type = BalancingDispatcher
      }
    """)

  val actorSystem = ActorSystem.create("TimeoutActorSystem",ConfigFactory.load(customConf))
  val actorA = actorSystem.actorOf(Props[ActorA],"actorA")
  var responseFuture = actorA ? DoCalculateActorA(6) mapTo manifest[Int]
  var response = Await.result(responseFuture, 35 seconds)
  println("Response received finally 1:" + response)

  val responseFuture2 = actorA ? DoCalculateActorA(10) mapTo manifest[Int]
  val response2 = Await.result(responseFuture2, 60 seconds)
  println("Response received finally 2:" + response2)


}

class ActorA extends Actor {

  val resizer = DefaultResizer(lowerBound = 1, upperBound = 15)
  //val router = context.actorOf(Props[ActorB].withRouter(RoundRobinRouter(resizer=Some(resizer))))
  //val router = context.actorOf(Props[ActorB].withRouter(RoundRobinRouter(1, routerDispatcher = "router")).withDispatcher("balancing-dispatcher"),"actorBRouter")

  implicit val timeout = Timeout(30 seconds)

  import context.dispatcher

  def receive = {
    case DoCalculateActorA(n) =>
      val router = context.actorOf(Props[ActorB].withRouter(RoundRobinRouter(n)))
      println("Sending futures to ActorB "+n+" times")
      val futures  =
        for (i <- 1 to n)
          yield {
          router ? DoCalculateActorB mapTo manifest[DoneFromActorB]
        }


    println("Done sending all futures to Actor B")
    val returnVal = Future.sequence(futures).map(f => f.foldLeft(0)((r1,r2) => r1 + r2.result))
    println("Piping futures back to sender from Actor A")
    returnVal pipeTo sender
  }

}

class ActorB extends Actor {

  //implicit val timeout = Timeout(4 seconds)
  import context.dispatcher

  def receive = {
    case DoCalculateActorB => println("Putting actor ("+self.path.name+","+hashCode+") to sleep for 3500ms")
    val future = Future {Thread.sleep(3500)}
    Await.result(future, 6 seconds)
    println("Future in Actor  ("+self.path.name+","+hashCode+")is Done. Returning")
    sender ! DoneFromActorB(4)
  }
}

case class DoCalculateActorA(numberOfCalcs:Int)
case object DoCalculateActorB
case class DoneFromActorB(result:Int)
case class CalculatedResult(result:Long)
