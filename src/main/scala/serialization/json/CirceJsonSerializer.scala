package serialization.json

import io.circe.{ Decoder, Encoder }

trait CirceJsonSerializer[A] extends JsonSerializer[A] {

  import io.circe.parser.{ decode => circeDecode }

  implicit def decoder: Decoder[A]
  implicit def encoder: Encoder[A]

  override implicit def encode(a: A): String = encoder(a).spaces2
  override implicit def decode(a: String): Either[String, A] = circeDecode[A](a)(decoder) match {
    case Right(a) => Right(a)
    case Left(a) => Left(a.getMessage)
  }
}
