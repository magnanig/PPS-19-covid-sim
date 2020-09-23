package pps.covid_sim.model.places.rooms

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.DelimitedSpace
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Dimension

case class BasicRoom(override val capacity: Int) extends Room {
  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
    RandomGeneration.randomIntInRange(2, 8), 50)

  override val mask: Option[Mask] = None
}