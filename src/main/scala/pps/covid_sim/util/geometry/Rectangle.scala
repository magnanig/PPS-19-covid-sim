package pps.covid_sim.util.geometry

case class Rectangle(topLeftCorner: Coordinates, bottomRightCorner: Coordinates) extends GeometryEntity {

  /**
   * The coordinates of rectangle's vertexes.
   */
  val vertexes: Set[Coordinates] = Set(
    topLeftCorner, (bottomRightCorner.x, topLeftCorner.y), (topLeftCorner.x, bottomRightCorner.y), bottomRightCorner
  )

  override def -(point: Coordinates): Double = {
    // thanks https://stackoverflow.com/a/18157551
    val dx = Math.max(0, Math.max(topLeftCorner.x - point.x, point.x - bottomRightCorner.x))
    val dy = Math.max(0, Math.max(topLeftCorner.y - point.y, point.y - bottomRightCorner.y))
    Math.sqrt(dx * dx + dy * dy)
  }
}
