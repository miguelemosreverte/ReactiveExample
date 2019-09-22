package object geojson {
  case class GeoPoint(latitude: Double, longitude: Double)

  case class Polygon(points: List[GeoPoint]) {
    def corners = points.size
    def horizontalCoordinates = points map (_.latitude)
    def verticalCoordinates = points map (_.longitude)
  }
}
