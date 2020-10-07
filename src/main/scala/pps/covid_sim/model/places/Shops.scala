package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.rooms.Room
import pps.covid_sim.parameters.CreationParameters.{clothesShopFillFactor, supermarketFillFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry._
import pps.covid_sim.util.scheduling.TimeTable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Shops {

  sealed trait Shop extends Room with ClosedWorkPlace[Shop] with LimitedHourAccess with MovementSpace {

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected[places] def canEnter(group: PeopleGroup.Group, time: Calendar): Boolean = {
      super[ClosedWorkPlace].canEnter(group, time) && super[LimitedHourAccess].canEnter(group, time)
    }

  }

  case class SuperMarket(override val city: City,
                         override val capacity: Int,
                         override val timeTable: TimeTable,
                         override val openedInLockdown: Boolean) extends Shop {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(5, 20), 2000)

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension, supermarketFillFactor)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.linearPathWithWallFollowing(dimension, obstacles, Speed.SLOW, 3)
  }

  case class ClothesShop(override val city: City,
                         override val capacity: Int,
                         override val timeTable: TimeTable,
                         override val openedInLockdown: Boolean) extends Shop {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(5, 20), 1000)

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension, clothesShopFillFactor)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.linearPathWithWallFollowing(dimension, obstacles, Speed.SLOW, 4)
  }

}
