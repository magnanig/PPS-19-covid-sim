package pps.covid_sim.model.container

import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}

object PeopleContainer {

  private var _people: List[Person] = List()

  //TODO scaladoc
  def add(person: Person): Unit = {
    _people = person :: _people
  }

  def add(people: List[Person]): Unit = {
    _people = _people ::: people
  }

  def getPeople(area: Area): List[Person] = area match {
    case province: Province => getPeople(province)
    case region: Region => getPeople(region)
    case city: City => getPeople(city)
    case _ => getPeople
  }

  def getPeople: List[Person] = _people

  private def getPeople(city: City): List[Person] = getPeople.filter(p => p.residence.equals(city))

  private def getPeople(province: Province): List[Person] =
    getPeople.filter(p => p.residence.province.equals(province))

  private def getPeople(region: Region): List[Person] =
    getPeople.filter(p => p.residence.province.region.equals(region))

  private[model] def checkAssignedWork(): Unit = {
    _people = _people.filter(person => myFilter(person))
  }

  private def myFilter(person: Person): Boolean = {
    if ((person.getClass == classOf[Worker] && person.asInstanceOf[Worker].workPlace == null) ||
      person.getClass == classOf[Student] && person.asInstanceOf[Student].institute == null)
      false
    else
      true
  }

}
