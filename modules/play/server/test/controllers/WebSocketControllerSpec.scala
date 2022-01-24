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

class WebSocketControllerSpec extends FunSuite with GuiceOneServerPerSuite with ScalaFutures {
  implicit val system = ActorSystem("dfd")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(15, Minutes), interval = Span(1, Seconds))

  val value = s"ws://localhost:$testServerPort/ws"
  val flow = Http().webSocketClientFlow(WebSocketRequest(akka.http.scaladsl.model.Uri(value)))
  test("pre actions") {
    val sink = Sink.foreach[Message] {
      case message: TextMessage.Strict ⇒
        println(message.text)
    }
    val (upgradeResponse, closed) =
      Source
        .maybe[TextMessage]
        .viaMat(flow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
        .toMat(sink)(Keep.both) // also keep the Future[Done]
        .run()

    // just like a regular http request we can access response status which is available via upgrade.response.status
    // status code 101 (Switching Protocols) indicates that server support WebSockets
    val connected = upgradeResponse.flatMap { upgrade ⇒
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    // in a real application you would not side effect here
    connected.futureValue
    println(closed.futureValue)

  }
}
