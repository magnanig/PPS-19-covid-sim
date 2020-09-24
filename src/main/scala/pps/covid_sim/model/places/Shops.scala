package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.people.{PeopleGroup, Person}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.rooms.Room
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Coordinates, Dimension}
import pps.covid_sim.util.scheduling.TimeTable

object Shops {

  sealed trait Shop extends Room with ClosedWorkPlace[Shop] with LimitedHourAccess with MovementSpace {

    override protected val movement: (Coordinates, Set[Person]) => (Coordinates) = ??? // TODO

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected[places] def canEnter(group: PeopleGroup.Group, time: Calendar): Boolean = {
      super[ClosedWorkPlace].canEnter(group, time) && super[LimitedHourAccess].canEnter(group, time)
    }

  }

  case class SuperMarket(override val city: City,
                         override val capacity: Int,
                         override val timeTable: TimeTable) extends Shop {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(5, 20), 2000)
  }

  case class ClothesShop(override val city: City,
                         override val capacity: Int,
                         override val timeTable: TimeTable) extends Shop {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(5, 20), 1000)
  }

}
