package ddd

object StringAggregateRoot {

  sealed trait StringAggregateRoot {
    def aggregateRoot: String
  }

  object StringAggregateRoot {

    trait Command extends StringAggregateRoot with AbstractCommand[String] {
      def deliveryId: BigInt
    }

    trait Query extends StringAggregateRoot with AbstractQuery[String]

    final case class GetState(aggregateRoot: String) extends Query

  }
}
