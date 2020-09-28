package pps.covid_sim.model.creation.test

import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.people.People._
import pps.covid_sim.model.people.Person
import pps.covid_sim.parameters.CreationParameters._
import pps.covid_sim.util.RandomGeneration._
import pps.covid_sim.util.Statistic

object PeopleObject {

  private val peopleCreation: PeopleObject = new PeopleObject()

  def people: List[Person] =  if (peopleCreation.people.isEmpty) peopleCreation.create() else peopleCreation.people

}

private class PeopleObject() {

  var people: List[Person] = List[Person]()

  def create(): List[Person] = {
    people = CitiesObject.getCities.toList.flatMap(city =>
      createPeople(city, workersPercentage, teachersPercentage, studentsPercentage, unemployedPercentage)
    )
    people
  }

  /**
   * Parameter values expressed as a percentage
   */
  def createPeople(city: City, workers: Double, teachers: Double, students: Double, unemployed: Double): List[Person] = {
    var people: List[Person] = List()
    val number: List[Int] = Statistic.totalPercentageToInt(city.numResidents, workers, teachers, students, unemployed)
    (1 to number.head).foreach(_ => people = people :+ Worker(randomBirthDate(), city))
    (1 to number(1)).foreach(_ => people = people :+ Teacher(randomBirthDate(), city))
    (1 to number(2)).foreach(_ => people = people :+ Student(randomBirthDate(), city))
    (1 to number(3)).foreach(_ => people = people :+ Unemployed(randomBirthDate(), city))
    people
  }

}