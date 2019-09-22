package geojson

import org.scalatest.{FlatSpec, Matchers}

class PointInPolygonSpec extends FlatSpec with Matchers {

  import geojson.GeoJsonData._

  "A given point inside Retiro Neighborhood in Buenos Aires" should "pointInPolygon return true" in {
    inRetiroNeighborhood(GeoPoint(-34.594261, -58.382435)) should be(true)
  }

  "A given point outside Retiro Neighborhood in Buenos Aires" should "pointInPolygon return false" in {
    inRetiroNeighborhood(GeoPoint(-34.5886021, -58.389601700000014)) should be(false)
  }

  "A given point inside Recoleta Neighborhood in Buenos Aires" should "pointInPolygon return true" in {
    inBarrioRecoletaNeighborhood(GeoPoint(-34.5886021, -58.389601700000014)) should be(true)
  }

  "A given point outside Barrio Norte Neighborhood in Buenos Aires" should "pointInPolygon return false" in {
    inBarrioNorteNeighborhood(GeoPoint(-34.590750, -58.397187)) should be(false)
  }

}