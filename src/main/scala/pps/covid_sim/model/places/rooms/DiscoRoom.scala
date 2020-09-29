package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.parameters.CreationParameters.{maxNumDiscoObstacles, minNumDiscoObstacles}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class DiscoRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(0.2, 2), 200)

  /**
   * Defines the room's obstacles, representing for example the bar counter, the cash desk, etc...
   * @param dimension the dimension of the current space
   * @return          the set of obstacles of the room
   */
  private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()
    val totObstacles = RandomGeneration.randomIntInRange(minNumDiscoObstacles, maxNumDiscoObstacles)
    @tailrec
    def _placeObstacles(): Unit = {
      val obstacle = generalIndoorObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
      else obstacles += obstacle
    }

    (0 until totObstacles).foreach(_ => _placeObstacles())
    obstacles
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
    MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)

}
