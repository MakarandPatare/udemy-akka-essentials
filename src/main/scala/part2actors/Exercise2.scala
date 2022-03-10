package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.Exercise2.Person.LiveTheLife

/**
 *    * 2. a Bank account as an actor
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
object Exercise2 extends App {
  val actorSystem = ActorSystem("exerciseActorSystem")

  // bank account
  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  class BankAccount extends Actor {
    import BankAccount._

    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0)
          sender ! TransactionFailure("Invalid deposit amount")
        else {
          funds += amount
          sender ! TransactionSuccess(s"successfully deposited $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0) sender ! TransactionFailure("Invalid withdraw amount")
        else if (amount > funds) sender ! TransactionFailure("Insufficient funds")
        else {
          funds -= amount
          sender ! TransactionSuccess(s"successfully withdrew $amount")
        }
      case Statement => sender ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {

    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val person = actorSystem.actorOf(Props[Person], "billionaire")

  person ! LiveTheLife(account)

}
