package pps.covid_sim.model.creation

import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.parameters.CreationParameters.{studentsPercentage, teachersPercentage, unemployedPercentage, workersPercentage}
import pps.covid_sim.util.Statistic
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Region}
import pps.covid_sim.util.RandomGeneration.randomBirthDate
import pps.covid_sim.model.people.People.{Student, Teacher, Unemployed, Worker}

object RegionPeopleCreation {

  private var people: List[Person] = List()

  def create(region: Region): List[Person] = { if (people.isEmpty) people = new RegionPeopleCreation(region).create()
    people
  }

  def getPeople: List[Person] = people

  private[creation] def checkAssignedWork(): Unit = {
    people = people.filter(person => myFilter(person))
  }

  private def myFilter(person: Person): Boolean = {
    if ((person.getClass == classOf[Worker] && person.asInstanceOf[Worker].workPlace == null) ||
      person.getClass == classOf[Student] && person.asInstanceOf[Student].institute == null)
      false
    else
      true
  }

}

private class RegionPeopleCreation(val region: Region) {

  private val _cities: List[City] = RegionCitiesCreation.create(region).toList
  var people: List[Person] = List()

  def create(): List[Person] = {
    CitiesContainer.add(_cities)
    people = _cities.flatMap(city => createPeople(city))
    people
  }

  /**
   * Parameter values expressed as a percentage
   */
  def createPeople(city: City): List[Person] = {
    var people: List[Person] = List()
    val number: List[Int] = computeProportions(city)

    (1 to number.head).foreach(_ => people = people :+ Worker(randomBirthDate(), city))
    (1 to number(1)).foreach(_ => people = people :+ Teacher(randomBirthDate(), city))
    (1 to number(2)).foreach(_ => people = people :+ Student(randomBirthDate(), city))
    (1 to number(3)).foreach(_ => people = people :+ Unemployed(randomBirthDate(), city))

    people
  }

  private def computeProportions(city: City): List[Int] = {
    if (city.numResidents > CreationParameters.minResidencesToSchool) {
      Statistic.totalPercentageToInt(city.numResidents,
        workersPercentage, teachersPercentage, studentsPercentage, unemployedPercentage)
    } else {
      Statistic.totalPercentageToInt(city.numResidents,
        workersPercentage + teachersPercentage, 0, 0, unemployedPercentage + studentsPercentage)
    }
  }

}
