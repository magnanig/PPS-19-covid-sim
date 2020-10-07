package pps.covid_sim.util.geometry

import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.{calculateFilling, generalIndoorObstacle, shopObstacle}

import scala.annotation.tailrec

object Obstacles {

  /**
   * Defines the desired obstacles, representing for example the bar counter, the cash desk, etc...
   * @param dimension   the dimension of the current space
   * @param min         the minimum number of obstacles
   * @param max         the maximum number of obstacles
   * @return            the set of obstacles of the room
   */
  def placeObstacles(dimension: Dimension, min: Int, max: Int): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()
    val totObstacles = RandomGeneration.randomIntInRange(min, max)
    @tailrec
    def _placeObstacles(): Unit = {
      val obstacle = generalIndoorObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
      else obstacles += obstacle
    }

    (0 until totObstacles).foreach(_ => _placeObstacles())
    obstacles
  }

  /**
   * Defines the desired obstacles, representing the shelves for goods.
   * @param dimension   the dimension of the current space
   * @param fillFactor  hte fill factor to be applied to the place
   * @return            the set of obstacles of the room
   */
  def placeObstacles(dimension: Dimension, fillFactor: Int): Set[Rectangle] = {
    var shelves: Set[Rectangle] = Set()
    val numOfShelves: Int = calculateFilling(dimension.width, fillFactor)

    (0 until numOfShelves).foreach(n => shelves += shopObstacle(dimension, n, fillFactor))
    shelves
  }

}
