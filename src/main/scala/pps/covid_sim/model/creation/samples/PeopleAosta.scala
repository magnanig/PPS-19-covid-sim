package pps.covid_sim.model.creation.samples

import pps.covid_sim.util.Statistic
import pps.covid_sim.model.people.People._
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.util.RandomGeneration.randomBirthDate
import pps.covid_sim.parameters.CreationParameters.{studentsPercentage, teachersPercentage, unemployedPercentage, workersPercentage}

object PeopleAosta {

  private val peopleCreation: PeopleAosta = new PeopleAosta()

  def people: List[Person] =  if (peopleCreation.people.isEmpty) peopleCreation.create() else peopleCreation.people

}

private class PeopleAosta() {

  var people: List[Person] = List[Person]()

  def create(): List[Person] = {
    people = CitiesAosta.getCities.toList.flatMap(city =>
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
    val childrenAndElderly: List[Int] = Statistic.totalPercentageToInt(number(3), 50, 50)

    (1 to number.head).foreach(_ => people = Worker(randomBirthDate(23, 68), city) :: people)
    (1 to number(1)).foreach(_ => people = Teacher(randomBirthDate(28, 68), city) :: people)
    (1 to number(2)).foreach(_ => people = Student(randomBirthDate(6, 24), city) :: people)
    (1 to childrenAndElderly.head).foreach(_ => people = people :+ Unemployed(randomBirthDate(0, 5), city))
    (1 to childrenAndElderly.last).foreach(_ => people = people :+ Unemployed(randomBirthDate(69, 100), city))

    people
  }

}
