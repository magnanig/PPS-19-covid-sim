package pps.covid_sim.util.geometry

import pps.covid_sim.util.RandomGeneration

/**
 * Possible directions considered in the movement functions, considering a point of view over the place.
 *
 * @param deltaX    the delta of variation along the x-axis
 * @param deltaY    the delta of variation along the y-axis
 */
case class Direction private(deltaX: Double, deltaY: Double)

object Direction {
  private[Direction] def apply(deltaX: Double, deltaY: Double): Direction = new Direction(deltaX, deltaY)

  val EAST: Direction = Direction(1.0, 0.0)
  val WEST: Direction = Direction(-1.0, 0.0)
  val NORTH: Direction = Direction(0.0, -1.0)
  val SOUTH: Direction = Direction(0.0, 1.0)
  val NORTH_EAST: Direction = Direction(1.0, -1.0)
  val NORTH_WEST: Direction = Direction(-1.0, -1.0)
  val SOUTH_EAST: Direction = Direction(1.0, 1.0)
  val SOUTH_WEST: Direction = Direction(-1.0, 1.0)

  /**
   *
   * @return    a random direction
   */
  def randomDirection(): Direction = RandomGeneration.randomIntInRange(0, 7) match {
      case 0 => Direction.EAST
      case 1 => Direction.WEST
      case 2 => Direction.NORTH
      case 3 => Direction.SOUTH
      case 4 => Direction.NORTH_EAST
      case 5 => Direction.NORTH_WEST
      case 6 => Direction.SOUTH_EAST
      case _ => Direction.SOUTH_WEST
    }
}
