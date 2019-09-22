package geojson


object GeoJsonData {

  val retiroNeighborhood = List(
    GeoPoint(-34.57643186942568, -58.38921547110658),
    GeoPoint(-34.57643186942568, -58.36320877249818),
    GeoPoint(-34.59876071694007, -58.36320877249818),
    GeoPoint(-34.59876071694007, -58.38921547110658),
    GeoPoint(-34.57643186942568, -58.38921547110658))

  val barrioNorteNeighborhood = List(
    GeoPoint(-34.57883865, -58.40449285),
    GeoPoint(-34.57883865, -58.395507150000014),
    GeoPoint(-34.58782135, -58.395507150000014),
    GeoPoint(-34.58782135, -58.40449285),
    GeoPoint(-34.57883865, -58.40449285))

  val barrioRecoletaNeighborhood = List(
    GeoPoint(-34.58286262736618, -58.40629577811342),
    GeoPoint(-34.58286262736618, -58.381919862586074),
    GeoPoint(-34.60017373322843, -58.381919862586074),
    GeoPoint(-34.60017373322843, -58.40629577811342),
    GeoPoint(-34.58286262736618, -58.40629577811342))

  val retiroNeighborhoodPolygon = Polygon(retiroNeighborhood)
  val barrioNorteNeighborhoodPolygon = Polygon(barrioNorteNeighborhood)
  val barrioRecoletaNeighborhoodPolygon = Polygon(barrioRecoletaNeighborhood)

  def inRetiroNeighborhood(point: GeoPoint) =
    PointInPolygon.pointInPolygon(point, retiroNeighborhoodPolygon)

  def inBarrioNorteNeighborhood(point: GeoPoint) =
    PointInPolygon.pointInPolygon(point, barrioNorteNeighborhoodPolygon)

  def inBarrioRecoletaNeighborhood(point: GeoPoint) =
    PointInPolygon.pointInPolygon(point, barrioRecoletaNeighborhoodPolygon)
}
