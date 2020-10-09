package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movement.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class BasicRoom(override val capacity: Int) extends Room with MovementSpace {
  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomIntInRange(2, 8), 50)

  override val obstacles: Set[Rectangle] = Set.empty

  override val mask: Option[Mask] = None

  override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
    MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 1)

}