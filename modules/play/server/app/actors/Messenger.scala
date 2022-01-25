package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}

object Messenger {
  def props(clientActorRef: ActorRef) = Props(new Messenger(clientActorRef))

//  implicit val messageFlowTransformer: MessageFlowTransformer[Message, Message] =
//    gracefulMessageFlowTransformer(messageFormat)
}

class Messenger(clientActorRef: ActorRef) extends Actor {
  val logger = play.api.Logger(getClass)

  // this is where we receive json messages sent by the client,
  // and send them a json reply
  def receive = {
    case jsValue: JsValue =>
      val clientMessage = getMessage(jsValue)
      val json: JsValue = Json.parse(s"""{"body": "You said, ‘$clientMessage’"}""")
      clientActorRef ! json
      Thread.sleep(1000)
      clientActorRef ! Json.parse(s"""{"body": "This is Additional Message for Original Message ‘$clientMessage’"}""")
      logger.info("Done responding")
  }

  // parse the "message" field from the json the client sends us
  def getMessage(json: JsValue): String = (json \ "message").as[String]

}
