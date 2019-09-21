package serialization

import play.api.libs.json.Json
import serialization.json.{JsonSerializer, PlayJsonSerializer}

object JsonFormats {


  object MessageSerializer {

    case class Message(value: String = "Done")

    class MessageEventSerializer extends EventSerializer[Message]

    implicit object Serializer extends PlayJsonSerializer[Message] with JsonSerializer[Message] {
      implicit override def oformat = Json.format[Message]
    }


  }


}
