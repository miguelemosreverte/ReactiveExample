package object ddd {

  trait Response {
    def deliveryId: BigInt
  }

  trait Event {
    def name: String
  }

  trait AbstractQuery[A] {
    def aggregateRoot: A
  }
  trait AbstractCommand[A] {
    def aggregateRoot: A
  }

  trait AbstractState {
    def +(event: Event): AbstractState
  }


}
