package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // part 1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  // word count actor
  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0
    //behavior
    def receive: PartialFunction[Any, Unit] = { // or : Receive (same type)
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part 3 - Instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part 4 - communicate - Asynchronous (sequence is not guaranteed)
  wordCounter ! "I am learning Akka and it's pretty damn cool!" // ! is pronounced as "tell"
  anotherWordCounter ! "A different message"

  // Actor with parameter
  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
    }
  }

  val person1 = actorSystem.actorOf(Person.props("Bob")) // better way - with companion object
  val person2 = actorSystem.actorOf(Props(new Person("Mark"))) // not encouraged

  person1 ! "hi"
  person2 ! "hi"
}
