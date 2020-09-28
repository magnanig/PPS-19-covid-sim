package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.{PeopleGroup, Person}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.rooms.Room
import pps.covid_sim.parameters.CreationParameters.{clothesShopFillFactor, supermarketFillFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.{calculateFilling, generalIndoorObstacle, shopObstacle}
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}
import pps.covid_sim.util.scheduling.TimeTable

object Shops {

  sealed trait Shop extends Room with ClosedWorkPlace[Shop] with LimitedHourAccess with MovementSpace {

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

    /**
     * Defines the supermarket obstacles, representing the shelves for goods.
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var shelves: Set[Rectangle] = Set()
      val numOfShelves: Int = calculateFilling(dimension.width, supermarketFillFactor)

      (0 until numOfShelves).foreach(n => shelves += shopObstacle(dimension, n, supermarketFillFactor))

      shelves
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.linearPath(dimension, obstacles)
  }

  case class ClothesShop(override val city: City,
                         override val capacity: Int,
                         override val timeTable: TimeTable) extends Shop {
    override val dimension: Dimension = DelimitedSpace.randomDimension(capacity,
      RandomGeneration.randomDoubleInRange(5, 20), 1000)

    /**
     * Defines the shop obstacles, representing the shelves for goods.
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var shelves: Set[Rectangle] = Set()
      val numOfShelves: Int = calculateFilling(dimension.width, clothesShopFillFactor)

      (0 until numOfShelves).foreach(n => shelves += shopObstacle(dimension, n, clothesShopFillFactor))

      shelves
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.linearPath(dimension, obstacles)
  }

}
