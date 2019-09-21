package serialization

import play.api.libs.json.Json
import serialization.json.{JsonSerializer, PlayJsonSerializer}

import scala.concurrent.Future

object JsonFormats {


    object MessageSerializer {

      case class Message(value: String = "Done")

      class MessageEventSerializer extends EventSerializer[Message]
      implicit val serializer : JsonSerializer[Message] = Serializer

      implicit object Serializer extends PlayJsonSerializer[Message] {
        implicit override def oformat = Json.format[Message]
      }



      object MessageSerializerTypeClass {


        trait Trait[A] {
          implicit def encode(a: A): String
          implicit def decode(a: String): Either[String, A]
        }

        def apply[A](implicit sh: Trait[A]): Trait[A] = sh

        def encode[A: Trait](a: A) = MessageSerializerTypeClass[A].encode(a)
        def decode[A: Trait](a: String) = MessageSerializerTypeClass[A].decode(a)

        implicit class EncodeOps[A: Trait](a: Message) {
          def encode = MessageSerializerTypeClass[Message].encode(a)
        }
        implicit class DecodeOps[A: Trait](a: String) {
          def decode = MessageSerializerTypeClass[A].decode(a)
        }
        implicit val implicitSerializer: Trait[Message] =
          new Trait[Message] {
            def encode(value: Message): String = serializer.encode(value)
            def decode(value: String): Either[String, Message] = serializer.decode(value)
          }

        Message("Example message").encode.decode

      }
    }






}
