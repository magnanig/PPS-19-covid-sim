package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.Field
import pps.covid_sim.model.places.rooms.{GymRoom, MultiRoom, Room}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}
import pps.covid_sim.util.scheduling.TimeTable

object Hobbies {

  trait Hobby[T <: Location] extends WorkPlace[T]

  case class FootballTeam(override val city: City,
                          fields: Set[Field]) extends Place with Hobby[Field] {
    override val mask: Option[Mask] = None
  }

  case class Gym(override val city: City,
                 override val timeTable: TimeTable,
                 private var rooms: Seq[GymRoom] = Seq())
    extends MultiRoom[GymRoom](city, rooms) with Hobby[GymRoom] with LimitedHourAccess {

  }

}
