package ddd


trait Command extends Product with Serializable {
  def aggregateRoot: String

  def deliveryId: BigInt
}

trait Query {
  def aggregateRoot: String
}

trait Response {
  def deliveryId: BigInt
}

trait Event {
  def name: String
}

final case class GetState(aggregateRoot: String) extends Query

trait AbstractState {
  def +(event: Event): AbstractState
}

