package pps.covid_sim.util.geometry

import pps.covid_sim.util.DoubleImplicits.RichDouble
import pps.covid_sim.util.RandomGeneration

case class Coordinates(x: Double, y: Double) extends GeometryEntity {

  /**
   * Check whether current point is on the border of the specified dimension.
   * @param dimension   the desired dimension
   * @return            true if current point if on the border, false otherwise
   */
  def onBorder(dimension: Dimension): Boolean = nearBorder(dimension)(0)

  /**
   * Check whether current point is near the border, within a max distance, of the specified dimension.
   * @param dimension     the desired dimension
   * @param maxDistance   the max allowed distance from border
   * @return              true if current point if near the border, false otherwise
   */
  def nearBorder(dimension: Dimension)(implicit maxDistance: Double): Boolean = x >= 0 && x <= dimension.width &&
    y >= 0 && y <= dimension.length && (x <= maxDistance || x >= dimension.width - maxDistance ||
    y <= maxDistance || (y >= dimension.length - maxDistance))

  /**
   * Check whether current point is inside the specified rectangle.
   * @param rectangle   the desired rectangle
   * @return            true if current point is inside the rectangle, false otherwise
   */
  def inside(rectangle: Rectangle): Boolean = rectangle - this == 0

  override def -(point: Coordinates): Double = Math.sqrt((x - point.x)^2 + (y - point.y)^2)
}

object Coordinates {
  private val margin = 0.5

  implicit def doubleTupleToDimension(dimension: (Double, Double)): Coordinates = Coordinates(dimension._1, dimension._2)

  implicit def intTupleToDimension(dimension: (Int, Int)): Coordinates = Coordinates(dimension._1, dimension._2)

  /**
   * Generate a random point on the border of the specified dimension.
   * @param dimension   the desired dimension
   * @return            a random point on the border
   */
  def randomOnBorder(dimension: Dimension): Coordinates = RandomGeneration.randomIntInRange(0, 3) match {
    case 0 => (RandomGeneration.randomDoubleInRange(margin, dimension.width - margin), 0.0)
    case 1 => (dimension.width, RandomGeneration.randomDoubleInRange(margin, dimension.length - margin))
    case 2 => (RandomGeneration.randomDoubleInRange(margin, dimension.width - margin), dimension.length)
    case _ => (0.0, RandomGeneration.randomDoubleInRange(margin, dimension.width - margin))
  }

  /**
   * Generate a random point inside the specified dimension
   * @param dimension   the dimension inside which generate a random point
   * @return            a random point inside dimension
   */
  def random(dimension: Dimension): Coordinates = Coordinates(
    RandomGeneration.randomDoubleInRange(margin, dimension.width - margin),
    RandomGeneration.randomDoubleInRange(margin, dimension.length - margin)
  )
}
