package actors

import julienrf.json.derived
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import java.util.UUID
import scala.concurrent.Future
import scala.reflect.runtime.universe

object JsonRPC {
  case class MessageMethod(method: String) extends scala.annotation.StaticAnnotation

  def annotationMethod(classSymbol: universe.ClassSymbol): String = {
    val annotations = classSymbol.annotations
    annotations
      .find(_.tree.tpe <:< universe.typeOf[MessageMethod])
      .flatMap { annotation ⇒
        val methodName = annotation.tree.children.tail.head
        methodName.collect {
          case universe.Literal(universe.Constant(value: String)) ⇒ value
        }.headOption
      }
      .getOrElse(throw new Exception("Missing annotation parameter"))
  }

  def subclasses[T: universe.TypeTag](): Set[universe.Symbol] = {
    val tpe = universe.typeOf[T]
    val clazz = tpe.typeSymbol.asClass
    clazz.knownDirectSubclasses
  }

  abstract class JsonRPCRequestMessageMethodFactory[T <: JsonRPCMessageMethod[_, _]: universe.TypeTag] {
    val methodToClassName: Map[String, String] = {
      val tpe = universe.typeOf[T]
      val clazz = tpe.typeSymbol.asClass
      val subclasses = clazz.knownDirectSubclasses
      subclasses.map(subclass ⇒ annotationMethod(subclass.asClass) → subclass.name.toString).toMap
    }
  }

  abstract class JsonRPCMessageMethod[State, Context] extends Product {
    lazy val method: String = annotationMethod(getType(getClass).typeSymbol.asClass)

    private def getType[T](clazz: Class[T]) = {
      val runtimeMirror = universe.runtimeMirror(clazz.getClassLoader)
      runtimeMirror.classSymbol(clazz).toType
    }

    def process(id: String, state: State, context: Context): Future[(State, ResponseMessage)]
    def printMessage(): String = this.method
  }

  trait JsonRPCRequestMessage {
    def id: String
    def method: JsonRPCMessageMethod[_, _]
  }

  // MESSAGES

  trait Message
  case class InvalidMessage(error: String) extends Message
  object RequestMessage {
    def apply[T <: JsonRPCMessageMethod[_, _]](method: T): RequestMessage[T] = apply(UUID.randomUUID().toString, method)
  }
  case class ProcessingSuccess(id: String, nextState: Messenger, response: ResponseMessage)
  case class ProcessingFailure(id: String, exception: Throwable)
  case class RequestMessage[T <: JsonRPCMessageMethod[_, _]](id: String, method: T) extends Message

  def formatRequestMessageFormat[T <: JsonRPCMessageMethod[_, _]: universe.TypeTag](
                                                                                     factory: JsonRPCRequestMessageMethodFactory[T],
                                                                                     msgFormat: Format[T]
                                                                                   ): Format[RequestMessage[T]] = new Format[RequestMessage[T]] {
    def reads(json: JsValue): JsResult[RequestMessage[T]] = {
      (JsPath \ "id")
        .read[String]
        .and((JsPath \ "method").read[String])
        .and((JsPath \ "params").readNullableWithDefault[JsObject](None)) { (id, method, params) ⇒
          val paramsWithType =
            params.getOrElse(JsObject.empty) + ("type" → JsString(factory.methodToClassName(method)))
          val paramsClass = paramsWithType.as[T](msgFormat)
          RequestMessage(id, paramsClass)
        }
    }.reads(json)

    def writes(requestMessage: RequestMessage[T]): JsObject = {
      val withoutParams = JsObject(
        Seq(
          "id" → JsString(requestMessage.id),
          "method" → JsString(requestMessage.method.method)
        )
      )

      if (requestMessage.method.productArity > 0) {
        withoutParams + ("params" → (Json.toJson(requestMessage.method)(msgFormat).as[JsObject] - "type"))
      } else {
        withoutParams
      }
    }
  }

  sealed trait ResponseMessage extends Message {
    def id: String
  }

  case class ResponseMessageError(code: Int, message: String, data: String)
  implicit val responseMessageErrorFormat: Format[ResponseMessageError] = derived.oformat()

  case class SuccessResponseMessage[T](id: String, result: T) extends ResponseMessage
  case class ErrorResponseMessage[T](id: String, error: T) extends ResponseMessage

  def formatResponseMessage[RESULT, ERROR](
                                            formatResult: Format[RESULT],
                                            formatError: Format[ERROR]
                                          ): Format[ResponseMessage] =
    new Format[ResponseMessage] {
      def reads(json: JsValue): JsResult[ResponseMessage] =
        (JsPath \ "id")
          .read[String]
          .and((JsPath \ "result").readNullable[RESULT](formatResult))
          .and((JsPath \ "error").readNullable[ERROR](formatError)) { (id, result, error) ⇒
            (result, error) match {
              case (Some(result), None) ⇒ SuccessResponseMessage(id, result)
              case (None, Some(error)) ⇒ ErrorResponseMessage(id, error)
              case _ ⇒ throw new Exception("")
            }
          }
          .reads(json)

      def writes(response: ResponseMessage): JsValue = response match {
        case success: SuccessResponseMessage[RESULT] ⇒
          implicit val implicitFormatResult: Format[RESULT] = formatResult
          Json.toJson(success)(Json.format[SuccessResponseMessage[RESULT]])
        case error: ErrorResponseMessage[ERROR] ⇒
          implicit val implicitFormatError: Format[ERROR] = formatError
          Json.toJson(error)(Json.format[ErrorResponseMessage[ERROR]])
      }
    }

  case class NotificationMessage[T <: JsonRPCMessageMethod[_, _]](method: T) extends Message

  def formatNotificationMessage[T <: JsonRPCMessageMethod[_, _]: universe.TypeTag](
                                                                                    factory: JsonRPCRequestMessageMethodFactory[T],
                                                                                    msgFormat: Format[T]
                                                                                  ): Format[NotificationMessage[T]] = new Format[NotificationMessage[T]] {
    def reads(json: JsValue): JsResult[NotificationMessage[T]] = {
      (JsPath \ "method")
        .read[String]
        .and((JsPath \ "params").readNullableWithDefault[JsObject](None)) { (method, params) ⇒
          val paramsWithType =
            params.getOrElse(JsObject.empty) + ("type" → JsString(factory.methodToClassName(method)))
          val paramsClass = paramsWithType.as[T](msgFormat)
          NotificationMessage(paramsClass)
        }
    }.reads(json)

    def writes(requestMessage: NotificationMessage[T]): JsObject = {
      val withoutParams = JsObject(Seq("method" → JsString(requestMessage.method.method)))
      if (requestMessage.method.productArity > 0) {
        withoutParams + ("params" → (Json.toJson(requestMessage.method)(msgFormat).as[JsObject] - "type"))
      } else {
        withoutParams
      }
    }
  }

  def formatGenericMessage[T <: JsonRPCMessageMethod[_, _], U <: JsonRPCMessageMethod[_, _]](
                                                                                              requestMessageFormat: Format[RequestMessage[T]],
                                                                                              responseMessageFormat: Format[ResponseMessage],
                                                                                              notificationMessageFormat: Format[NotificationMessage[U]]
                                                                                            ): Format[Message] = new Format[Message] {
    def reads(json: JsValue): JsResult[Message] = {
      (JsPath \ "id")
        .readNullable[String]
        .and((JsPath \ "result").readNullable[JsValue])
        .and((JsPath \ "error").readNullable[JsValue])
        .and((JsPath \ "method").readNullable[JsValue]) { (id, result, error, method) ⇒
          (id, result, error, method) match {
            case (Some(_), Some(_), None, None) ⇒ json.as[ResponseMessage](responseMessageFormat)
            case (Some(_), None, Some(_), None) ⇒ json.as[ResponseMessage](responseMessageFormat)
            case (Some(_), None, None, Some(_)) ⇒ json.as[RequestMessage[T]](requestMessageFormat)
            case (None, None, None, Some(_)) ⇒ json.as[NotificationMessage[U]](notificationMessageFormat)
            case _ ⇒ throw new Exception()
          }
        }
        .reads(json)
    }

    def writes(message: Message): JsValue = message match {
      case request: RequestMessage[T] ⇒ Json.toJson(request)(requestMessageFormat)
      case response: ResponseMessage ⇒ Json.toJson(response)(responseMessageFormat)
      case notification: NotificationMessage[U] ⇒ Json.toJson(notification)(notificationMessageFormat)
      case _ ⇒ throw new Exception()
    }
  }
}
