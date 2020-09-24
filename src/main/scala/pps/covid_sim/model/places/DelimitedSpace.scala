package pps.covid_sim.model.places

import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Dimension, Rectangle}

trait DelimitedSpace extends Location {

  /**
   * The dimension of current space.
   */
  val dimension: Dimension
  val obstacles: Set[Rectangle]

  /**
   * Get the groups that have entered into current location.
   * @return  the set of group inside the current location
   */
  def spaceDimension: Dimension = dimension

  /**
   *
   * @param dimension the dimension of current space
   * @return
   */
  def placeObstacles(dimension: Dimension): Set[Rectangle]

}

object DelimitedSpace {
  /**
   * Create a random dimension (width and length) starting from the space capacity
   * and the desired square meters per person. The dimension will be such that surface
   * will be at least 10 square meters, and both width and length will be at least 3 meters.
   * @param capacity        the delimited space capacity (either approximate or maximum)
   * @param sqmPerPerson    the desired square meters per person
   * @param surfaceLimit    a limit for the generated surface, in square meters (sqm)
   * @return                a dimension (width and length) big enough for containing all
   *                        the desired people, each with his "personal area"
   */
  def randomDimension(capacity: Int, sqmPerPerson: Double, surfaceLimit: Double): Dimension = {
    val surface = Math.min(surfaceLimit, Math.max(10, capacity * sqmPerPerson)) // 10 <= surface <= surfaceLimit
    val side = Math.sqrt(surface) // at least >3 meters
    val width = Math.max(3, RandomGeneration.randomDoubleFromGaussian(side, 2))
    Dimension(width, surface / width)
  }
}
