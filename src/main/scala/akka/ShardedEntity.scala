package akka

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import ddd.{AbstractCommand, AbstractQuery}
import ddd.GeoAggregateRoot.GeoAggregateRoot
import ddd.StringAggregateRoot.StringAggregateRoot.{Command, Query}
import geojson.GeoJsonData._
import geojson.GeoPoint

trait ShardedEntity {

  val typeName: String
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case qry: AbstractQuery[_] => (qry.aggregateRoot.toString, qry)
    case cmd: AbstractCommand[_] => (cmd.aggregateRoot.toString, cmd)
  }

  def props(): Props

  // Factory Method for AggregateObjeto
  def start(implicit system: ActorSystem): ActorRef = ClusterSharding(system).start(
    typeName        = typeName,
    entityProps     = props(),
    settings        = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId  = extractShardId(3 * 10)
  )

  def extractShardId(numberOfShards: Int): ShardRegion.ExtractShardId = {
    case geo: GeoAggregateRoot => geo match {
      case _ if inRetiroNeighborhood(geo.aggregateRoot) => "1"
      case _ if inBarrioNorteNeighborhood(geo.aggregateRoot) => "2"
      case _ if inBarrioRecoletaNeighborhood(geo.aggregateRoot) => "3"
      case _ => ((geo.aggregateRoot.latitude * geo.aggregateRoot.longitude).toLong % numberOfShards).toString
    }
    case qry: Query => (qry.aggregateRoot.toLong % numberOfShards).toString
    case cmd: Command => (cmd.aggregateRoot.toLong % numberOfShards).toString
  }
}



