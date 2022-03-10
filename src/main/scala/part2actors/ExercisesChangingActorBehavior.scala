package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import jdk.internal.org.jline.reader.Candidate

/**
 * 1 - recreate the Counter Actor with context.become and NO MUTABLE STATE
 * 2 - simplified voting system
 */
object ExercisesChangingActorBehavior extends App {

  val system = ActorSystem("exerciseChangingActorBehavior")

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[countReceive($currentCount)]: Incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"[countReceive($currentCount)]: Decrementing")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my current count is $currentCount")
    }
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5) foreach(_ => counter ! Increment)
  (1 to 3) foreach(_ => counter ! Decrement)
  counter ! Print

  /**
   * Exercise 2 - Simplified voting system
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
    var candidate: Option[String] = None
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c)) // candidate = Some(c)
      case VoteStatusRequest => sender ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
//    var stillWaiting: Set[ActorRef] = Set()
//    var currentStats: Map[String, Int] = Map()
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender ! VoteStatusRequest // Citizen has not voted yet and this might end up in infinite loop if any citizen doesn't vote
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty)
          println(s"[aggregator] poll stats: $newStats")
        else
          context.become(awaitingStatuses(newStillWaiting, newStats))
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
}
