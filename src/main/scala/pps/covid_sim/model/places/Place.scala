package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.geometry.Dimension

/**
 * A generic place where people can go (can be either a closed building or opened place)
 */
trait Place extends Location {

  val city: City

}
