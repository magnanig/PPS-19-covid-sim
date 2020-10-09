package pps.covid_sim.model.creation.samples

import pps.covid_sim.model.Statistic
import pps.covid_sim.model.creation.CitiesObject
import pps.covid_sim.model.people.People._
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.parameters.CreationParameters._
import pps.covid_sim.util.RandomGeneration._

import scala.util.Random

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
    val random: Random = new Random()
    val number: List[Int] = Statistic.totalPercentageToInt(city.numResidents, workers, teachers, students, unemployed)
    val childrenAndElderly: List[Int] = Statistic.totalPercentageToInt(number(3), 50, 50)

    (1 to number.head).foreach(_ => people = people :+ Worker(randomBirthDate(23, 68, random), city))
    (1 to number(1)).foreach(_ => people = people :+ Teacher(randomBirthDate(28, 68, random), city))
    (1 to number(2)).foreach(_ => people = people :+ Student(randomBirthDate(6, 24, random), city))
    (1 to childrenAndElderly.head).foreach(_ => people = people :+ Unemployed(
      randomBirthDate(0, 5, random), city))
    (1 to childrenAndElderly.last).foreach(_ => people = people :+ Unemployed(
      randomBirthDate(69, 100, random), city))
    people
  }

}