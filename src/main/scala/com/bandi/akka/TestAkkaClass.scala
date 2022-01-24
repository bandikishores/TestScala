package com.bandi.akka

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt;
class HelloAkka extends Actor { // Extending actor trait
  var count = 0

  def receive = { //  Receiving message
    case msg: String => {
      count = count + 1
      println(msg + " " + self.path.name + " " + count)
      Thread.sleep(200L)

//      var childActor = context.actorOf(Props[HelloAkka], "Child" + count);
//      childActor ! "Hello"
//      context.stop(childActor)

      val child = context.actorOf(Props[ActorChildReplyExample],"ActorChild");
      child ! "Hello Child"
    }
    case _ => println("Unknown message") // Default case
  }
//
//  override def preStart() { // overriding preStart method
//    println("preStart method is called");
//  }
//  override def postStop() { // Overriding postStop method
//    println("postStop method is called");
//  }
}

class ActorChildReplyExample extends Actor{
  def receive ={
    case message:String => println("Message recieved from "+sender.path.name+" massage: "+message);
      println("Replying to "+sender().path.name);
      sender()! "I got you message";
  }
}

object Main {
  def main(args: Array[String]) {
    var actorSystem = ActorSystem("ActorSystem"); // Creating ActorSystem
    var actor =
      actorSystem.actorOf(Props[HelloAkka], "FirstActor") //Creating actor

    actor ! "Hello Akka " // Sending messages by using !
    printf("%s\n", "Sent message")
    Thread.sleep(10L)

//    implicit val timeout = Timeout(2 seconds);
//    val future = actor ? "Hello";
//    val result = Await.result(future, timeout.duration);
//    println("Message received: "+result);

    //actorSystem.stop(actor)
  }
}
