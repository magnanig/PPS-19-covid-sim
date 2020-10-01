package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.parameters.CreationParameters.{maxGymObstaclesFactor, minGymObstaclesFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class GymRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(4, 9), 200)

  /**
   * Defines the gym room's obstacles, representing for example the gym equipment and machinery.
   * @param dimension the dimension of the current space
   * @return          the set of obstacles of the room
   */
  private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()
    val minObstacles: Int = (dimension.surface / minGymObstaclesFactor).toInt
    val maxObstacles: Int = (dimension.surface / maxGymObstaclesFactor).toInt
    val totObstacles = RandomGeneration.randomIntInRange(minObstacles, maxObstacles)
    println("TOT OBSTACLE: " + totObstacles)
    @tailrec
    def _placeObstacle(): Unit = {
      val obstacle = generalIndoorObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) {println("DUP"); _placeObstacle()}
      else {println("OBSTACLE INSERTED"); obstacles += obstacle }
    }

    (0 until totObstacles).foreach(_ => _placeObstacle())
    obstacles
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
    MovementFunctions.linearPathWithWallFollowing(dimension, obstacles, Speed.SLOW, 2)

}
