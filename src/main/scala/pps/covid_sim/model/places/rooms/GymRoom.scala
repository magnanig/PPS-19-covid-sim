package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.parameters.CreationParameters.{maxGymObstaclesFactor, minGymObstaclesFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}

case class GymRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(4, 9), 200)

  /**
   * Defines the gym room's obstacles, representing for example the gym equipment and machinery.
   * @param dimension the dimension of the current space
   * @return          the set of obstacles of the room
   */
  override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()
    val minObstacles: Int = (dimension.surface / minGymObstaclesFactor).toInt
    val maxObstacles: Int = (dimension.surface / maxGymObstaclesFactor).toInt
    val totObstacles = RandomGeneration.randomIntInRange(minObstacles, maxObstacles)

    println("TOT OBSTACLE: " + totObstacles)

    def _placeObstacles(): Unit = {
      val obstacle = generalIndoorObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) {println("DUP"); _placeObstacles()}
      else {println("OBSTACLE INSERTED"); obstacles += obstacle }
    }
    (0 until totObstacles).foreach(_ => _placeObstacles())

    obstacles
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
    MovementFunctions.linearPath(dimension, obstacles)

}
