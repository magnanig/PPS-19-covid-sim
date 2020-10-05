package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.places.Locations.LimitedPeopleLocation
import pps.covid_sim.model.places.Place

/**
 * A generic means of transport that people can take (can be either a public or private transport).
 */
trait Transport extends LimitedPeopleLocation with Place{

  /**
   * Method that defines the dynamics of the virus propagation between different groups of people.
   * @param place     the place where people are
   * @param time      current time
   */
  def extraGroupVirusPropagation(place: Place, time: Calendar)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    synchronized {
      currentGroups
        .flatMap(group => group.toList)
        .toList
        .combinations(2)
        .foreach(pair => if (Single(pair.head).leader != Single(pair.last).leader){
          VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time)
        })
    }
  }

  /**
   * Propagate virus inside the current means of transport, considering the infection between different groups and
   * extending the basic implementation that considers infections within the same group.
   * @param time  current time
   * @param place current place
   */
  override def propagateVirus(time: Calendar, place: Place)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    super.propagateVirus(time, place)(covidInfectionParameters)
    extraGroupVirusPropagation(place, time)(covidInfectionParameters)
  }

}
