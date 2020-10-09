package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.parameters.CreationParameters.{maxGymObstaclesFactor, minGymObstaclesFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class GymRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(4, 9), 200)

  override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension,
    (dimension.surface / minGymObstaclesFactor).toInt, (dimension.surface / maxGymObstaclesFactor).toInt)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
    MovementFunctions.linearPathWithWallFollowing(dimension, obstacles, Speed.SLOW, 2)

}
