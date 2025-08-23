package ir.uni.polaris.model

data class Measurement(
  val location: GeoPoint,
  val cell: CellSnapshot?
)
