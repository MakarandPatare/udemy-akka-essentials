package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    // internal state of the kid
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender ! KidAccept
        else sender ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false) // change my receive handler to sadReceive - state stack is maintained when discardOld is false
      case Food(CHOCOLATE) => // stay happy
      case Ask(_) => sender ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome() // change the state back to the previous one from the stack
      case Ask(_) => sender ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String) // question like do you want to play
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        //kidRef ! Food(CHOCOLATE) // stateless kid will also become happy if we uncomment this
        kidRef ! Ask("do you want to play?")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("My kid is sad, but at lest he's healthy")
    }
  }

  val actorSystem = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = actorSystem.actorOf(Props[FussyKid])
  val statelessFussyKid = actorSystem.actorOf(Props[StatelessFussyKid])
  val mom = actorSystem.actorOf(Props[Mom])

  mom ! MomStart(fussyKid)
  mom ! MomStart(statelessFussyKid)
}
