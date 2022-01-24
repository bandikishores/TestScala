package controllers

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Minutes, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WebSocketControllerTest extends FunSuite with GuiceOneServerPerSuite with ScalaFutures {
  implicit val system = ActorSystem("dfd")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(15, Seconds), interval = Span(1, Seconds))

  val value = s"ws://localhost:9001/ws"
  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(akka.http.scaladsl.model.Uri(value)))
  test("pre actions") {

    val outgoing =
      Source.single(TextMessage(s"""{"message":"Hello World"}"""))

    var receivedOutput = false

    def generate() : Boolean = {
      for(i ← 1 to 100) {
        if(receivedOutput) {
          return true
        }
        Thread.sleep(300)
      }
      false
    }

    val dummyFuture = Future[Boolean] {
      generate()
    }

    val incoming =
      Sink.foreach[Message] {
        case message: TextMessage.Strict =>
          println("Value received from Server " + message.text)
          receivedOutput = true
        case value ⇒
          println("Incoming Value isn't TextMessage? : " + value)
      }

    // upgradeResponse is a Future[WebSocketUpgradeResponse]
    // and it's expected to complete with success or failure
    val (upgradeResponse, closed) =
    outgoing
      .viaMat(webSocketFlow)(Keep.right)
      .toMat(incoming)(Keep.both)
      .run()

    upgradeResponse.onComplete {
      case t: Throwable =>
        println(s"Connection failed : ${t.getMessage}")
      case value ⇒
        println("Successful? Maybe : " + value)
        if (value.get.response.status == StatusCodes.SwitchingProtocols) {
          Future.successful(Done)
        } else {
          throw new RuntimeException(s"Connection failed: ${value.get.response.status}")
        }
    }

    println(closed.futureValue)
   // println(dummyFuture.futureValue)

//    val sink = Sink.foreach[Message] {
//      case message: TextMessage.Strict ⇒
//        println(message.text)
//      case value ⇒
//        println("Oops unknown format " + value)
//    }
//    val (upgradeResponse, closed) =
//      Source
//        .maybe[TextMessage]
//        .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
//        .toMat(sink)(Keep.both) // also keep the Future[Done]
//        .run()
//
//    // just like a regular http request we can access response status which is available via upgrade.response.status
//    // status code 101 (Switching Protocols) indicates that server support WebSockets
//    val connected = upgradeResponse.flatMap { upgrade ⇒
//      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
//        Future.successful(Done)
//      } else {
//        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
//      }
//    }
//
//    upgradeResponse.onComplete {
//      case t: Throwable =>
//        println(s"Connection failed : ${t.getMessage}")
//      case value ⇒
//        println("Successful? Maybe : " + value)
//        if (value.get.response.status == StatusCodes.SwitchingProtocols) {
//          Future.successful(Done)
//        } else {
//          throw new RuntimeException(s"Connection failed: ${value.get.response.status}")
//        }
//    }
//
//    // in a real application you would not side effect here
//    connected.futureValue
//    println(closed.futureValue)

  }
}
