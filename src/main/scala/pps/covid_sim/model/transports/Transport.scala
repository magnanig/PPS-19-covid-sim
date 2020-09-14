package pps.covid_sim.model.transports

import pps.covid_sim.model.people.PeopleGroup.{Group, Single}
import pps.covid_sim.model.places.Locations.LimitedPeopleLocation
import pps.covid_sim.util.time.Time.Day.Day

/**
 * A generic means of transport that people can take (can be either a public or private transport)
 */
trait Transport extends LimitedPeopleLocation {

}
