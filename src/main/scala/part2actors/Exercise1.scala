package part2actors

import akka.actor.{Actor, ActorSystem, Props}
import part2actors.Exercise1.Counter.{Decrement, Increment, Print}

/**
 *    * 1. a Counter actor which holds an internal variable
 *      - Increment
 *      - Decrement
 *      - Print
 */
object Exercise1 extends App {
  val actorSystem = ActorSystem("exerciseActorSystem")

  // DOMAIN of the counter
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  val counter = actorSystem.actorOf(Props[Counter], "myCounter")

  1 to 5 foreach(_ => counter ! Increment)
  1 to 3 foreach(_ => counter ! Decrement)
  counter ! Print
}
