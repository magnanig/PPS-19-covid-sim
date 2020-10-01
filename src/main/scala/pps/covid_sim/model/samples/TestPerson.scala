package pps.covid_sim.model.samples

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.util.RandomGeneration

case class TestPerson(override val residence: City,
                      override val isInfected: Boolean) extends Person {
  override val birthDate: Calendar = RandomGeneration.randomBirthDate()
}
