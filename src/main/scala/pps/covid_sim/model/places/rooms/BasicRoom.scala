package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}

import scala.annotation.tailrec

case class BasicRoom(override val capacity: Int) extends Room with MovementSpace {
  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomIntInRange(2, 8), 50)

  /**
   * Defines up to ten room's obstacles, representing for example ...
   * @param dimension the dimension of current space
   * @return          the set of obstacles of the room
   */
  private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()

    @tailrec
    def _placeObstacles(): Unit = {
      val obstacle = generalIndoorObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
      else obstacles += obstacle
    }
    (0 until RandomGeneration.randomIntInRange(0, 9)).foreach(_ => _placeObstacles())

    obstacles
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  override val mask: Option[Mask] = None

  override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
    MovementFunctions.randomPath(dimension, obstacles)

}