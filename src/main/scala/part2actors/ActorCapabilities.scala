package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender ! "Hello, there!" // or simply 'sender()' - replying to a message
      case message: String => println(s"[${context.self}] I have received: $message") // or just self
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case contents: SpecialMessage => println(s"[simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s")
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // a. messages must be IMMUTABLE
  // b. messages must be SERIALIZABLE
  // in practice use case classes and case objects

  simpleActor ! 42 // who is the sender?

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self means 'this' in OOP

  case class SendMessageToYourself(message: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!" // reply to "me" which is null - called deadLetters actors

  // 5 - forwarding messages
  // D -> A -> B
  // forwarding - sending a message with ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) // no sender

  /**
   * Exercises
   *
   * 1. a Counter actor which holds an internal variable
   *      - Increment
   *      - Decrement
   *      - Print
   *
   * 2. a Bank account as an actor
   *      receives
   *      - Deposit an amount
   *      - Withdraw an amount
   *      - Statement
   *      replies with
   *      - Success
   *      - Failure
   *
   *      interact with some other kind of actor
   */
}
