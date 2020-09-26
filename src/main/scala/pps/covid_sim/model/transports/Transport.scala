package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.clinical.VirusPropagation.tryInfect
import pps.covid_sim.model.people.PeopleGroup.{Group, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.LimitedPeopleLocation
import pps.covid_sim.model.places.Place

import scala.util.Random

/**
 * A generic means of transport that people can take (can be either a public or private transport)
 */
trait Transport extends LimitedPeopleLocation {

  def extraGroupVirusPropagation(place: Place, time: Calendar): Unit = synchronized {
    currentGroups
      .flatMap(group => group.toList)
      .toList
      .combinations(2)
      .foreach(pair => if (Single(pair.head).leader != Single(pair.last).leader) tryInfect(pair.head, pair.last, place, time))
  }

  /**
   * Propagate virus inside the current means of transport, considering the infection between different groups and
   * extending the basic implementation that considers infections within the same group.
   *
   * @param time  current time
   * @param place current place
   */
  override def propagateVirus(time: Calendar, place: Place): Unit = {
    super.propagateVirus(time, place)
    extraGroupVirusPropagation(place, time)
  }

}
