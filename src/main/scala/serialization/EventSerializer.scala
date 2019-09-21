package serialization

import java.io.NotSerializableException
import java.nio.charset.Charset

import akka.serialization.SerializerWithStringManifest
import serialization.json.JsonSerializer

import scala.reflect.ClassTag

/** Simplest possible serializer, uses a string representation of the Person class.
  *
  * Usually a serializer like this would use a library like:
  * protobuf, kryo, avro, cap'n proto, flatbuffers, SBE or some other dedicated serializer backend
  * to perform the actual to/from bytes marshalling.
  */
abstract class EventSerializer[A <: AnyRef: ClassTag](implicit serializer: JsonSerializer[A])
  extends SerializerWithStringManifest { self =>

  override def identifier: Int = EventSerializer.identify(self.getClass.getName)

  private val Utf8 = Charset.forName("UTF-8")

  // extract manifest to be stored together with serialized object
  override def manifest(o: AnyRef): String = o.getClass.getName

  // serialize the object
  override def toBinary(obj: AnyRef): Array[Byte] = {
    obj match {
      case p: A =>
        val str = serializer.encode(p)
        str.getBytes(Utf8)
      case _ => throw new IllegalArgumentException(s"Unable to serialize to bytes, clazz was: ${obj.getClass}!")
    }
  }

  // deserialize the object, using the manifest to indicate which logic to apply
  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    val asString = new String(bytes, Utf8)
    serializer.decode(asString) match {
      case Right(a) => a
      case Left("Failed to decode") =>
        throw new NotSerializableException(
          s"Unable to deserialize from bytes, manifest was: $manifest! Bytes length: " + bytes.length
        )
      case Left(customMessage) =>
        throw new NotSerializableException(customMessage)
    }
  }
}

object EventSerializer {

  import serialization.JsonFormats.JsonModel.MessageSerializer.MessageEventSerializer
  private val map = Seq(
    classOf[MessageEventSerializer],
  )
    .map { _.getName }
    .zipWithIndex
    .map { case (k, v) => k -> (v + 100) } // this is due to the first 20 ids are reserved by akka
    .toMap

  def identify(s: String): Int =
    map(s)

}
