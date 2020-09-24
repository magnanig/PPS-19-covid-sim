package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.{generalObstacle, shelfObstacle}
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}

case class DiscoRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(0.2, 2), 200)

  /**
   * Defines up to three disco room's obstacles, representing for example the bar counter, the cash desk, etc...,
   * @param dimension the dimension of the current space
   * @return the set of obstacles of the room
   */
  override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
    var obstacles: Set[Rectangle] = Set()

    def _placeObstacles(): Unit = {
      val obstacle = generalObstacle(dimension)
      if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
      obstacles += obstacle
    }
    (0 until RandomGeneration.randomIntInRange(0, 3)).foreach(_ => _placeObstacles())

    obstacles
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Coordinates => Set[Seq[Map[Person, Seq[Int]]]] =
    MovementFunctions.randomPath(dimension, Set.empty)


}
