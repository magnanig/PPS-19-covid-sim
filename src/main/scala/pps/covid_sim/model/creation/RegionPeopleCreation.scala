package pps.covid_sim.model.creation

import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.People.{Student, Teacher, Unemployed, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Region}
import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.parameters.CreationParameters.{studentsPercentage, teachersPercentage, unemployedPercentage, workersPercentage}
import pps.covid_sim.util.RandomGeneration.randomBirthDate
import pps.covid_sim.util.Statistic

object RegionPeopleCreation {

  def create(area: Area): List[Person] = area match {
    case city: City => ??? // TODO CitiesPeopleCreation(city).create() <- creare SOLO le persone della CITTA' richiesta
    case region: Region => new RegionPeopleCreation(region).create()
    case _ => CitiesContainer.getRegions.flatMap(new RegionPeopleCreation(_).create()).toList
  }

}

private class RegionPeopleCreation(val region: Region) {

  private val _cities: Set[City] = RegionCitiesCreation.create(region)
  var people: List[Person] = List()

  def create(): List[Person] = {
    CitiesContainer.add(_cities)
    people = _cities.toList.flatMap(city => createPeople(city))
    people
  }

  def createPeople(city: City): List[Person] = {
    var people: List[Person] = List()
    val number: List[Int] = computeProportions(city)
    val childrenAndElderly: List[Int] = Statistic.totalPercentageToInt(number(3), 50, 50)

    (1 to number.head).foreach(_ => people = people :+ Worker(randomBirthDate(23, 68), city))
    (1 to number(1)).foreach(_ => people = people :+ Teacher(randomBirthDate(28, 68), city))
    (1 to number(2)).foreach(_ => people = people :+ Student(randomBirthDate(6, 24), city))
    (1 to childrenAndElderly.head).foreach(_ => people = people :+ Unemployed(randomBirthDate(0, 5), city))
    (1 to childrenAndElderly.last).foreach(_ => people = people :+ Unemployed(randomBirthDate(69, 100), city))

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
