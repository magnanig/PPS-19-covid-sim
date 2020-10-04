package pps.covid_sim.util.geometry

import pps.covid_sim.util.DoubleImplicits.RichDouble
import pps.covid_sim.util.RandomGeneration

/**
 * Utility class that performs different operations on a point.
 * @param x     the x-axis coordinate of the point
 * @param y     the y-axis coordinate of the point
 */
case class Coordinates(x: Double, y: Double) extends GeometryEntity {

  /**
   * Check whether current point is on the border of the specified dimension.
   * @param dimension   the desired dimension
   * @return            true if current point is on the border, false otherwise
   */
  def onBorder(dimension: Dimension): Boolean = nearBorder(dimension)(0)

  /**
   * Check whether current point is near the border, within a max distance, of the specified dimension.
   * @param dimension     the desired dimension
   * @param maxDistance   the max allowed distance from border
   * @return              true if current point is near the border, false otherwise
   */
  def nearBorder(dimension: Dimension)(implicit maxDistance: Double): Boolean = x >= 0 && x <= dimension.width &&
    y >= 0 && y <= dimension.length && (x <= maxDistance || x >= dimension.width - maxDistance ||
    y <= maxDistance || (y >= dimension.length - maxDistance))

  /**
   * Check whether current point is on a corner of the specified rectangle.
   * @param rectangle     the specified rectangle
   * @return              true if current point is on a corner of the rectangle, false otherwise
   */
  def onCorner(rectangle: Rectangle): Boolean = (x == rectangle.topLeftCorner.x && y == rectangle.topLeftCorner.y) ||
    (x == rectangle.topLeftCorner.x && y == rectangle.bottomRightCorner.y) ||
    (x == rectangle.bottomRightCorner.x && y == rectangle.bottomRightCorner.y) ||
    (x == rectangle.bottomRightCorner.x && y == rectangle.topLeftCorner.y)

  /**
   * Check whether current point is out of the specified dimension.
   * @param dimension     the desired dimension
   * @return              true if current point is out of the specified dimension, false otherwise
   */
  def outOfDimension(dimension: Dimension): Boolean = x < 0 || y < 0 || x > dimension.width || y > dimension.length

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
   * @return            a random point on the border, with at least a margin of 0.5 meters from any corner
   */
  def randomOnBorder(dimension: Dimension): Coordinates = RandomGeneration.randomIntInRange(0, 3) match {
    case 0 => (RandomGeneration.randomDoubleInRange(margin, dimension.width - margin), 0.0)
    case 1 => (dimension.width, RandomGeneration.randomDoubleInRange(margin, dimension.length - margin))
    case 2 => (RandomGeneration.randomDoubleInRange(margin, dimension.width - margin), dimension.length)
    case _ => (0.0, RandomGeneration.randomDoubleInRange(margin, dimension.width - margin))
  }

  /**
   * Generate a random point inside the specified dimension.
   * @param dimension   the dimension inside which generate a random point
   * @return            a random point inside dimension, with at least a margin of 0.5 meters from any border
   */
  def random(dimension: Dimension): Coordinates = Coordinates(
    RandomGeneration.randomDoubleInRange(margin, dimension.width - margin),
    RandomGeneration.randomDoubleInRange(margin, dimension.length - margin)
  )

  /**
   * Generate a random point inside the specified dimension, near the actual point.
   * @param dimension   the dimension inside which generate a random point
   * @param point       the starting point from which to calculate the new random point
   * @param speed       the speed of the moving point
   * @return            a random point inside dimension, close to the starting point
   */
  def randomClose(dimension: Dimension, point: Coordinates, speed: Speed): Coordinates =
    RandomGeneration.randomIntInRange(0, 7) match {
      case 0 => (point.x + (Direction.NORTH.deltaX * speed.delta), point.y + (Direction.NORTH.deltaY * speed.delta))
      case 1 => (point.x + (Direction.SOUTH.deltaX * speed.delta), point.y + (Direction.SOUTH.deltaY * speed.delta))
      case 2 => (point.x + (Direction.EAST.deltaX * speed.delta), point.y + (Direction.EAST.deltaY * speed.delta))
      case 3 => (point.x + (Direction.WEST.deltaX * speed.delta), point.y + (Direction.WEST.deltaY * speed.delta))
      case 4 => (point.x + (Direction.NORTH_EAST.deltaX * speed.delta), point.y + (Direction.NORTH_EAST.deltaY * speed.delta))
      case 5 => (point.x + (Direction.SOUTH_EAST.deltaX * speed.delta), point.y + (Direction.SOUTH_EAST.deltaY * speed.delta))
      case 6 => (point.x + (Direction.NORTH_WEST.deltaX * speed.delta), point.y + (Direction.NORTH_WEST.deltaY * speed.delta))
      case _ => (point.x + (Direction.SOUTH_WEST.deltaX * speed.delta), point.y + (Direction.SOUTH_WEST.deltaY * speed.delta))
  }

  /**
   * Generate a point inside the specified dimension, near the actual point, towards the specified direction.
   * @param point       the starting point from which to calculate the new close point
   * @param speed       the speed of the moving point
   * @param direction   the direction towards which the point moved
   * @return            a point inside the specified dimension, close to the actual point, towards the specified direction
   */
  def directionClose(point: Coordinates, speed: Speed, direction: Direction): Coordinates =
    direction match {
      case Direction.NORTH => (point.x + (Direction.NORTH.deltaX * speed.delta), point.y + (Direction.NORTH.deltaY * speed.delta))
      case Direction.SOUTH => (point.x + (Direction.SOUTH.deltaX * speed.delta), point.y + (Direction.SOUTH.deltaY * speed.delta))
      case Direction.EAST => (point.x + (Direction.EAST.deltaX * speed.delta), point.y + (Direction.EAST.deltaY * speed.delta))
      case Direction.WEST => (point.x + (Direction.WEST.deltaX * speed.delta), point.y + (Direction.WEST.deltaY * speed.delta))
      case Direction.NORTH_EAST => (point.x + (Direction.NORTH_EAST.deltaX * speed.delta), point.y + (Direction.NORTH_EAST.deltaY * speed.delta))
      case Direction.SOUTH_EAST => (point.x + (Direction.SOUTH_EAST.deltaX * speed.delta), point.y + (Direction.SOUTH_EAST.deltaY * speed.delta))
      case Direction.NORTH_WEST => (point.x + (Direction.NORTH_WEST.deltaX * speed.delta), point.y + (Direction.NORTH_WEST.deltaY * speed.delta))
      case Direction.SOUTH_WEST => (point.x + (Direction.SOUTH_WEST.deltaX * speed.delta), point.y + (Direction.SOUTH_WEST.deltaY * speed.delta))
    }

  /**
   * Translate the point on the border of the specified rectangle, towards the direction the point comes from.
   * @param point       the starting point from which to calculate the translated point
   * @param direction   the direction towards which the point moved
   * @param rectangle   the rectangle on whose border to translate the point
   * @return            the translated point
   */
  def translateOnBorder(point: Coordinates, direction: Direction, rectangle: Rectangle): Coordinates =
    direction match {
      case Direction.NORTH => (point.x, rectangle.bottomRightCorner.y)
      case Direction.SOUTH => (point.x, rectangle.topLeftCorner.y)
      case Direction.EAST => (rectangle.topLeftCorner.x, point.y)
      case Direction.WEST => (rectangle.bottomRightCorner.x, point.y)
      case Direction.NORTH_EAST => (rectangle.topLeftCorner.x, rectangle.bottomRightCorner.y)
      case Direction.SOUTH_EAST => (rectangle.topLeftCorner.x, rectangle.topLeftCorner.y)
      case Direction.NORTH_WEST => (rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)
      case Direction.SOUTH_WEST => (rectangle.bottomRightCorner.x, rectangle.topLeftCorner.y)
  }

  /**
   * Generate a point near the actual point, towards the specified direction, following the rectangle border clockwise.
   * @param point       the starting point from which to calculate the new close point
   * @param rectangle   the rectangle on whose border to calculate the new point
   * @return            a point close to the actual point, towards the specified direction,
   *                    following the rectangle border clockwise
   */
  def followBorder(point: Coordinates, speed: Speed, rectangle: Rectangle): Coordinates = point match {
    case Coordinates(rectangle.topLeftCorner.x, rectangle.topLeftCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.SOUTH)
      if (nextPoint.y > rectangle.bottomRightCorner.y) Coordinates(rectangle.topLeftCorner.x, rectangle.bottomRightCorner.y)
      else nextPoint
    case Coordinates(rectangle.topLeftCorner.x, rectangle.bottomRightCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.EAST)
      if (nextPoint.x > rectangle.bottomRightCorner.x) Coordinates(rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)
      else nextPoint
    case Coordinates(rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.NORTH)
      if (nextPoint.y < rectangle.topLeftCorner.y) Coordinates(rectangle.bottomRightCorner.x, rectangle.topLeftCorner.y)
      else nextPoint
    case Coordinates(rectangle.bottomRightCorner.x, rectangle.topLeftCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.WEST)
      if (nextPoint.x < rectangle.topLeftCorner.x) Coordinates(rectangle.topLeftCorner.x, rectangle.topLeftCorner.y)
      else nextPoint
    case Coordinates(rectangle.topLeftCorner.x, _) =>
      val nextPoint = directionClose(point, speed, Direction.SOUTH)
      if (nextPoint.y > rectangle.bottomRightCorner.y) Coordinates(rectangle.topLeftCorner.x, rectangle.bottomRightCorner.y)
      else nextPoint
    case Coordinates(_, rectangle.bottomRightCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.EAST)
      if (nextPoint.x > rectangle.bottomRightCorner.x) Coordinates(rectangle.bottomRightCorner.x, rectangle.bottomRightCorner.y)
      else nextPoint
    case Coordinates(rectangle.bottomRightCorner.x, _) =>
      val nextPoint = directionClose(point, speed, Direction.NORTH)
      if (nextPoint.y < rectangle.topLeftCorner.y) Coordinates(rectangle.bottomRightCorner.x, rectangle.topLeftCorner.y)
      else nextPoint
    case Coordinates(_, rectangle.topLeftCorner.y) =>
      val nextPoint = directionClose(point, speed, Direction.WEST)
      if (nextPoint.x < rectangle.topLeftCorner.x) Coordinates(rectangle.topLeftCorner.x, rectangle.topLeftCorner.y)
      else nextPoint
  }

}
