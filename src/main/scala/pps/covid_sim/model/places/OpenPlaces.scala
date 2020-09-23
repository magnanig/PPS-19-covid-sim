package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Coordinates, Dimension, MovementFunctions}
import pps.covid_sim.util.scheduling.TimeTable

object OpenPlaces {

  trait OpenPlace extends Place with MovementSpace {
    override lazy val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    // must be lazy since dimension will be defined after this trait initialization
    protected override lazy val movement: (Coordinates, Set[Person]) => Coordinates =
      MovementFunctions.randomPath(dimension, Set.empty)

    override def mask: Option[Mask] = if(dimension.surface / numCurrentPeople < 0) //Parameters.safeSurfacePerPerson) TODO
      Some(Masks.Surgical) else None
  }

  case class Beach(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 200),
      RandomGeneration.randomIntInRange(50, 80)
    )
  }

  case class Square(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 150),
      RandomGeneration.randomIntInRange(50, 150)
    )

    override val mask: Option[Mask] = Some(Masks.Surgical)
  }

  case class Park(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(20, 80),
      RandomGeneration.randomIntInRange(20, 80)
    )
  }

  case class Field(override val city: City,
                   override val timeTable: TimeTable) extends OpenPlace with LimitedHourAccess {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(90, 120),
      RandomGeneration.randomIntInRange(45, 90)
    )
  }

}
