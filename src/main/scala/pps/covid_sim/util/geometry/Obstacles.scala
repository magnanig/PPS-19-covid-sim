package pps.covid_sim.util.geometry

import pps.covid_sim.util.RandomGeneration

import scala.annotation.tailrec

object Obstacles {

  /**
   * Randomly generates the coordinates of an obstacle to be placed inside a room.
   * @return  a Rectangle representing a general obstacle inside the room
   */
  val generalIndoorObstacle: Dimension => Rectangle = roomDimension => {
    val topLeftX = RandomGeneration.randomDoubleInRange(0.5, roomDimension.width - 1.5)
    val topLeftY = RandomGeneration.randomDoubleInRange(0.5, roomDimension.length - 1.5)
    val bottomRightX = RandomGeneration.randomDoubleInRange(topLeftX + 0.3, topLeftX + 1.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0,
      Math.min(topLeftY + 3.0, roomDimension.length - 0.5))
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Randomly generates the coordinates of an outdoor obstacle (e.g. a tree, a bush, a fountain, a bench).
   * @return  a Rectangle representing a general obstacle in the open place
   */
  val generalOutdoorObstacle: Dimension => Rectangle = placeDimension => {
    val topLeftX = RandomGeneration.randomDoubleInRange(0.5, placeDimension.width - 3.5)
    val topLeftY = RandomGeneration.randomDoubleInRange(0.5, placeDimension.length - 3.5)
    val bottomRightX = RandomGeneration.randomDoubleInRange(topLeftX + 0.3, topLeftX + 3.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, topLeftY + 3.0)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Generates the coordinates of an obstacle (e.g. a shelf) according to the dimension of the room.
   * @return  a Rectangle representing a shelf in a shop or supermarket
   */
  val shopObstacle: (Dimension, Int, Int) => Rectangle = (roomDimension, position, fillFactor) => {
    val topLeftX = (position * fillFactor) + (fillFactor / 2.0)
    val topLeftY = RandomGeneration.randomDoubleInRange(1.0, roomDimension.length - 1.0)
    val bottomRightX = topLeftX + (fillFactor / 2.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, roomDimension.length - 1.0)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Defines the desired obstacles, representing for example the bar counter, the cash desk, etc...
   * @param dimension   the dimension of the current space
   * @param min         the minimum number of obstacles
   * @param max         the maximum number of obstacles
   * @return            the set of obstacles of the room
   */
  def placeObstacles(dimension: Dimension, min: Int, max: Int)
                    (implicit obstacleCreation: Dimension => Rectangle): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()
    val totObstacles = RandomGeneration.randomIntInRange(min, max)
    @tailrec
    def _placeObstacles(): Unit = {
      val obstacle = obstacleCreation(dimension)
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
  def placeObstacles(dimension: Dimension, fillFactor: Int)
                    (implicit obstacleCreation: (Dimension, Int, Int) => Rectangle): Set[Rectangle] = {
    var shelves: Set[Rectangle] = Set()
    val tot: Int = calculateFilling(dimension.width, fillFactor)

    (0 until tot).foreach(n => shelves += obstacleCreation(dimension, n, fillFactor))
    shelves
  }

  /**
   * Calculates the number of obstacles to be placed in the specified axis, in order to fill the place.
   * @param dimension       the size of the axis on which to calculate the filling of the obstacles
   * @param fillFactor      the fill factor to be applied to the place
   * @return                the number of obstacles that can be inserted along this axis to make the filling
   */
  private def calculateFilling(dimension: Double, fillFactor: Int): Int = {
    if ((dimension % fillFactor) == 0) ((dimension / fillFactor) - 1).toInt
    else (dimension / fillFactor).toInt
  }

}
