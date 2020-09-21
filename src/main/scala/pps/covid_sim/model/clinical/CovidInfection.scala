package pps.covid_sim.model.clinical

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Place

case class CovidInfection(on: Calendar, at: Place, person: Person) {

}