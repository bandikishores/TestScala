import actors.JsonRPC.{JsonRPCMessageMethod, NotificationMessage, RequestMessage, ResponseMessage}
import akka.stream.scaladsl.Flow
import play.api.http.websocket.{BinaryMessage, CloseCodes, CloseMessage, TextMessage, WebSocketCloseException, Message => WSMessage}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import play.api.libs.streams.AkkaStreams
import play.api.mvc.WebSocket.MessageFlowTransformer

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

package object actors {

//  implicit val messageFormat: Format[JsonRPC.Message] =
//    formatGenericMessage(null, null, null)
//
//  def formatGenericMessage[T <: JsonRPCMessageMethod[_, _], U <: JsonRPCMessageMethod[_, _]](
//                                                                                              requestMessageFormat: Format[RequestMessage[T]],
//                                                                                              responseMessageFormat: Format[ResponseMessage],
//                                                                                              notificationMessageFormat: Format[NotificationMessage[U]]
//                                                                                            ): Format[JsonRPC.Message] = new Format[JsonRPC.Message] {
//    def reads(json: JsValue): JsResult[JsonRPC.Message] = {
//      (JsPath \ "id")
//        .readNullable[String]
//        .and((JsPath \ "result").readNullable[JsValue])
//        .and((JsPath \ "error").readNullable[JsValue])
//        .and((JsPath \ "method").readNullable[JsValue]) { (id, result, error, method) ⇒
//          (id, result, error, method) match {
//            case (Some(_), Some(_), None, None) ⇒ json.as[ResponseMessage](responseMessageFormat)
//            case (Some(_), None, Some(_), None) ⇒ json.as[ResponseMessage](responseMessageFormat)
//            case (Some(_), None, None, Some(_)) ⇒ json.as[RequestMessage[T]](requestMessageFormat)
//            case (None, None, None, Some(_)) ⇒ json.as[NotificationMessage[U]](notificationMessageFormat)
//            case _ ⇒ throw new Exception()
//          }
//        }
//        .reads(json)
//    }
//
//    def writes(message: JsonRPC.Message): JsValue = message match {
//      case request: RequestMessage[T] ⇒ Json.toJson(request)(requestMessageFormat)
//      case response: ResponseMessage ⇒ Json.toJson(response)(responseMessageFormat)
//      case notification: NotificationMessage[U] ⇒ Json.toJson(notification)(notificationMessageFormat)
//      case _ ⇒ throw new Exception()
//    }
//  }
//
//  implicit val jsonFlowTransformer: MessageFlowTransformer[JsValue, JsValue] = {
//    new MessageFlowTransformer[JsValue, JsValue] {
//      def invalidOnException(block: ⇒ JsValue): Either[JsValue, CloseMessage] =
//        try Left(block)
//        catch {
//          case NonFatal(_) ⇒ Left(JsObject(Seq("invalid" → JsString("invalid"))))
//        }
//
//      def transform(flow: Flow[JsValue, JsValue, _]): Flow[WSMessage, WSMessage, _] = {
//        AkkaStreams.bypassWith[WSMessage, JsValue, WSMessage](Flow[WSMessage].collect {
//          case BinaryMessage(data) ⇒ invalidOnException(Json.parse(data.iterator.asInputStream))
//          case TextMessage(text) ⇒ invalidOnException(Json.parse(text))
//        })(flow.map { json ⇒
//          TextMessage(Json.stringify(json))
//        })
//      }
//    }
//  }
//
//  def gracefulMessageFlowTransformer(messageFormat: Format[JsonRPC.Message]): MessageFlowTransformer[JsonRPC.Message, JsonRPC.Message] = {
//    jsonFlowTransformer.map(
//      json ⇒ {
//        json match {
//          case obj: JsObject if obj.value.contains("invalid") ⇒
//            InvalidMessage(obj.value("invalid").toString())
//          case _ ⇒
//            Try(Json.fromJson[JsonRPC.Message](json)(messageFormat)) match {
//              case Success(value) ⇒
//                value.fold(
//                  { errors ⇒
//                    throw WebSocketCloseException(
//                      CloseMessage(Some(CloseCodes.Unacceptable), Json.stringify(JsError.toJson(errors)))
//                    )
//                  },
//                  identity
//                )
//              case Failure(exception) ⇒
//                InvalidMessage(exception.getMessage)
//            }
//        }
//      },
//      out ⇒ Json.toJson(out)(messageFormat)
//    )
//  }

  trait Message
  case class InvalidMessage(error: String) extends Message
}
