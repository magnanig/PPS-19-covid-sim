package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.parameters.CreationParameters._
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry._
import pps.covid_sim.util.scheduling.TimeTable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object OpenPlaces {

  trait OpenPlace extends Place with MovementSpace {

    implicit val obstacleCreation: Dimension => Rectangle = Obstacles.generalOutdoorObstacle

    override def mask: Option[Mask] = {
      val safeSurfacePerPerson = 3 * 3
      if (dimension.surface / numCurrentPeople < safeSurfacePerPerson)
        Some(Masks.Surgical) else None
    }
  }


  case class Square(override val city: City, override val openedInLockdown: Boolean) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 150),
      RandomGeneration.randomIntInRange(50, 150)
    )

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension,
      (dimension.surface / minSquareObstaclesFactor).toInt, (dimension.surface / maxSquareObstaclesFactor).toInt)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 4)
  }


  case class Park(override val city: City, override val openedInLockdown: Boolean) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(20, 80),
      RandomGeneration.randomIntInRange(20, 80)
    )

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension,
      (dimension.surface / minParkObstaclesFactor).toInt, (dimension.surface / maxParkObstaclesFactor).toInt)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 3)
  }


  case class Field(override val city: City,
                   override val timeTable: TimeTable,
                   override val openedInLockdown: Boolean) extends OpenPlace with LimitedHourAccess {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(90, 120),
      RandomGeneration.randomIntInRange(45, 90)
    )

    override val obstacles: Set[Rectangle] = Set.empty

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)
  }

}
