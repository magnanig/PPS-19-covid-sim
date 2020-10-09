package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.parameters.CreationParameters.{maxNumDiscoObstacles, minNumDiscoObstacles}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class DiscoRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(0.1, 1), 200)

  override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension, minNumDiscoObstacles, maxNumDiscoObstacles)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
    MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)

}
