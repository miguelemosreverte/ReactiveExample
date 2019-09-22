package ddd

import geojson.GeoPoint

object GeoAggregateRoot {
  sealed trait GeoAggregateRoot {
    def aggregateRoot: GeoPoint
  }

  object GeoAggregateRoot {

    trait Command extends GeoAggregateRoot with AbstractCommand[GeoPoint]{
      def deliveryId: BigInt
    }

    trait Query extends GeoAggregateRoot with AbstractQuery[GeoPoint]

    final case class GetState(aggregateRoot: GeoPoint) extends Query

  }
}
