package pps.covid_sim.model.places.arranging

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.DelimitedSpace
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.arranging.Placement.{Arrangement, ItemGroup, Placeholder}
import pps.covid_sim.model.places.rooms.Room

/**
 * Represents a delimited space with some type of disposition (e.g. tables disposition)
 * @tparam T  the type of a group of items to be positioned (e.g. tables group in a restaurant)
 * @tparam A  the type of the item assignee
 */
trait Arrangeable[A, T <: ItemGroup] extends DelimitedSpace {

  protected val arrangement: Arrangement[T]

  /**
   * Propagates the virus inside each item group assigned to some group.
   * @param time    current time
   * @param place   current place
   */
  override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    arrangement.rows
      .flatMap(_.itemGroups)
      .filter(!_.isFree)
      .foreach(_.propagateVirus(place, time)(covidInfectionParameters))
  }

  /**
   * Find an accommodation for the specified group
   * @param group   the desired group
   * @return        an optional pair with the assigned room and the sequence of placeholder,
   *                if any
   */
  protected[places] def findAccommodation(group: Group): Option[(Room, Seq[Placeholder[A]])]

  private[model] override def clear(): Unit = {
    super.clear()
    arrangement.rows.flatMap(_.itemGroups).foreach(_.release())
  }
}
