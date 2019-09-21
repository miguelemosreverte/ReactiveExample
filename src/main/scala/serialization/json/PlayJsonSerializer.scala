package serialization.json

import play.api.libs.json.{ Json, OFormat }

trait PlayJsonSerializer[A] extends JsonSerializer[A] {

  implicit def oformat: OFormat[A]

  override implicit def encode(a: A): String = Json.prettyPrint(oformat.writes(a))
  override implicit def decode(a: String): Either[String, A] =
    Json.parse(a).asOpt[A] match {
      case Some(a) => Right(a)
      case None => Left("Failed to decode")
    }
}
