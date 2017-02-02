package flightsaggregator.core.http.json

import java.util.UUID

import flightsaggregator.core.http.Error
import spray.json.{JsString, _}

import scala.util.Try

trait FlightAggregatorJsonProtocol extends DefaultJsonProtocol {
  def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] = new JsonFormat[T] {
    override def read(json: JsValue): T = construct(jr.read(json))

    override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
  }

  implicit val uuidFormat = new JsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(deserializationError("Expected UUID format"))
      case _              => deserializationError("Expected UUID format")
    }
  }

  implicit val errorJsonFormat = new RootJsonFormat[Error] {
    override def write(obj: Error): JsValue = {
      val msg = obj.logUuid match {
        case Some(uuid) => s"${obj.message}. Please refer to this error as ${uuid.toString}."
        case _          => obj.message
      }
      JsObject("message" -> JsString(msg))
    }
    override def read(json: JsValue): Error = {
      json.asJsObject.getFields("message") match {
        case Seq(JsString(message)) => Error(message)
        case _                      => deserializationError(deserializationErrorMessage)
      }
    }

    private val deserializationErrorMessage = "Slack-arch Error could not be created from given string"
  }
}

object FlightAggregatorJsonProtocol extends FlightAggregatorJsonProtocol
