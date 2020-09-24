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
  def generalObstacle(roomDimension: Dimension): Rectangle = {
    val topLeftX = RandomGeneration.randomDoubleInRange(0.0, roomDimension.width - 1.0)
    val topLeftY = RandomGeneration.randomDoubleInRange(0.0, roomDimension.length - 1.0)
    val bottomRightX = RandomGeneration.randomDoubleInRange(topLeftX + 0.3, topLeftX + 1.0)
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, roomDimension.length)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

  /**
   *  Generates the coordinates of an obstacle (e.g. a shelf) according to the obstacles
   *  already inserted in the room.
   * @param roomDimension the dimension of the room
   * @param position      the n-th shelf to be inserted inside the room
   * @return              a Rectangle representing a shelf in a shop or supermarket
   */
  def shelfObstacle(roomDimension: Dimension, position: Int): Rectangle = {
    val topLeftX = (position * 2) + 1.0
    val topLeftY = RandomGeneration.randomDoubleInRange(1.0, roomDimension.length - 1.0)
    val bottomRightX = topLeftX + 1.0
    val bottomRightY = RandomGeneration.randomDoubleInRange(topLeftY + 1.0, roomDimension.length)
    Rectangle((topLeftX, topLeftY), (bottomRightX, bottomRightY))
  }

}
