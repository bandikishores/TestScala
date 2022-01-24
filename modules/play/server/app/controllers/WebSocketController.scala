package controllers

import actors.Messenger
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import javax.inject.Inject

class WebSocketController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  val logger: Logger = play.api.Logger(getClass)

  // call this to display index.scala.html
  def websocket: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.websocket())
  }

  // the WebSocket
  def ws: WebSocket =
    WebSocket.accept[JsValue, JsValue] { _ =>
      ActorFlow.actorRef { actorRef =>
        logger.info("ws: calling My Actor")
        Messenger.props(actorRef)
      }
    }
}
