package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.Field
import pps.covid_sim.model.places.rooms.{MultiRoom, Room}
import pps.covid_sim.util.scheduling.TimeTable

object Hobbies {

  trait Hobby[T <: Location] extends WorkPlace[T]

  case class FootballTeam(override val city: City,
                          fields: Set[Field]) extends Place with Hobby[Field] {
    override val mask: Option[Mask] = None
  }

  case class Gym(override val city: City,
                 override val timeTable: TimeTable,
                 private var rooms: Seq[Room] = Seq())
    extends MultiRoom[Room](city, rooms) with Hobby[Room] with LimitedHourAccess {
    override val mask: Option[Mask] = None
  }

}
