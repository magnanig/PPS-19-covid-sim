package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.{DelimitedSpace, MovementSpace}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Coordinates, Dimension, MovementFunctions}

case class DiscoRoom(override val capacity: Int) extends Room with MovementSpace {

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomDoubleInRange(0.2, 2), 200)

  override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

  override val mask: Option[Mask] = Some(Masks.Surgical)

  override protected val movement: Coordinates => Coordinates = MovementFunctions.randomPath(dimension)
}
