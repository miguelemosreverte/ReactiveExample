package serialization.json

trait JsonSerializer[A] {
  implicit def encode(a: A): String
  implicit def decode(a: String): Either[String, A]

}
