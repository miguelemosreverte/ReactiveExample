package utils.generators

import geojson.GeoPoint

object RandomGeoPoints {

  def minimalChange = 0 + scala.util.Random.nextInt( (1 - 0) + 1 )

  def inRecoleta = {
    GeoPoint(-34.5886023 + minimalChange, -58.389601700000015 + minimalChange)
  }
  def inBarrioNorte = {
    GeoPoint(-34.590750 + minimalChange, -58.397187 + minimalChange)
  }
  def inRetiro = {
    GeoPoint(-34.5886021 + minimalChange, -58.389601700000014 + minimalChange)
  }
}
