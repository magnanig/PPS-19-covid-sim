package pps.covid_sim.util.geometry

import pps.covid_sim.util.RandomGeneration

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

object Rectangle {

  /**
   * Randomly generates the coordinates of an obstacle to be placed inside a room.
   * @param roomDimension the dimension of the room
   * @return              a Rectangle representing a general obstacle inside the room
   */
  def generalIndoorObstacle(roomDimension: Dimension): Rectangle = {
    val topLeftX = RandomGeneration.randomDoubleInRange(0.5, roomDimension.width - 1.5)
    val topLeftY = RandomGeneration.randomDoubleInRange(0.5, roomDimension.length - 1.5)
    val bottomRightX = RandomGeneration.randomDoubleInRange(topLeftX + 0.3, topLeftX + 1.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0,
      Math.min(topLeftY + 3.0, roomDimension.length - 0.5))
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Randomly generates the coordinates of an outdoor obstacle (e.g. a tree, a bush, a fountain, a bench).
   * @param placeDimension the dimension of the open place
   * @return               a Rectangle representing a general obstacle in the open place
   */
  def generalOutdoorObstacle(placeDimension: Dimension): Rectangle = {
    val topLeftX = RandomGeneration.randomDoubleInRange(0.5, placeDimension.width - 3.5)
    val topLeftY = RandomGeneration.randomDoubleInRange(0.5, placeDimension.length - 3.5)
    val bottomRightX = RandomGeneration.randomDoubleInRange(topLeftX + 0.3, topLeftX + 3.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, topLeftY + 3.0)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   *  Generates the coordinates of an obstacle (e.g. a shelf) according to the dimension of the room.
   * @param roomDimension the dimension of the room
   * @param position      the n-th shelf to be inserted inside the room
   * @param fillFactor    the fill factor to be applied to the shop
   * @return              a Rectangle representing a shelf in a shop or supermarket
   */
  def shopObstacle(roomDimension: Dimension, position: Int, fillFactor: Int): Rectangle = {
    val topLeftX = (position * fillFactor) + (fillFactor / 2.0)
    val topLeftY = RandomGeneration.randomDoubleInRange(1.0, roomDimension.length - 1.0)
    val bottomRightX = topLeftX + (fillFactor / 2.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, roomDimension.length - 1.0)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Generates the coordinates of a 2x2 meter square obstacle (i.e. an umbrella) filling the place.
   * @param rowPos      the row position of the obstacle
   * @param columnPos   the column position of the obstacle
   * @param fillFactor  the fill factor to be applied at the beach
   * @return            a Rectangle representing the square obstacle at the beach
   */
  def beachObstacle(rowPos: Int, columnPos: Int, fillFactor: Int): Rectangle = {
    val topLeftX = (columnPos * fillFactor) + (fillFactor / 2.0)
    val topLeftY = (rowPos * fillFactor) + (fillFactor / 2.0)
    val bottomRightX = topLeftX + (fillFactor / 2.0)
    val bottomRightY = topLeftY + (fillFactor / 2.0)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   * Calculates the number of obstacles to be placed in the specified axis, in order to fill the place.
   * @param dimension       the size of the axis on which to calculate the filling of the obstacles
   * @param fillFactor      the fill factor to be applied to the place
   * @return                the number of obstacles that can be inserted along this axis to make the filling
   */
  def calculateFilling(dimension: Double, fillFactor: Int): Int = {
    if ((dimension % fillFactor) == 0) ((dimension / fillFactor) - 1).toInt
    else (dimension / fillFactor).toInt
  }


}
