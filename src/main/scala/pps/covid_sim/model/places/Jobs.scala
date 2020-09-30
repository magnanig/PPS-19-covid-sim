package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.clinical.{Masks, VirusPropagation}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.rooms.{MultiRoom, Room}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Dimension, Rectangle}

object Jobs {

  case class Company(override val city: City,
                     private var offices: Seq[Office] = Seq())
    extends MultiRoom[Office](city, offices) with ClosedWorkPlace[Office]{
    override val mask: Option[Mask] = Some(Masks.FFP2WithoutValve)
  }

  case class Factory(override val city: City,
                     private var offices: Seq[Office] = Seq())
    extends MultiRoom[Office](city, offices) with ClosedWorkPlace[Office] {
    override val mask: Option[Mask] = Some(Masks.FFP2WithoutValve)
  }

  case class Office(override val capacity: Int) extends Room {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(3, 10), 40)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override def propagateVirus(time: Calendar, place: Place): Unit = currentGroups
      .flatMap(_.people)
      .toList
      .combinations(2)
      .foreach(pair => VirusPropagation.tryInfect(pair.head, pair.last, place, time))

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    /**
     *
     * @param dimension the dimension of current space
     * @return          the set of obstacles of the office
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = Set.empty
  }

}
