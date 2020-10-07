package pps.covid_sim.model.container

import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}

object PeopleContainer {

  private var _people: List[Person] = List()

  /**
   * Delete all people created so far.
   */
  def reset(): Unit = {
    _people = List()
  }

  /**
   * Adds a person to the container.
   */
  def add(person: Person): Unit = {
    _people = person :: _people
  }

  /**
   * Adds a list of people to the container.
   */
  def add(people: List[Person]): Unit = {
    _people = _people ::: people
  }

  /**
   * Get all the people who live in a specific area.
   *
   * @param area  area where the required people live.
   * @return      people who live in a specific area.
   */
  def getPeople(area: Area): List[Person] = area match {
    case province: Province => getPeople(province)
    case region: Region => getPeople(region)
    case city: City => getPeople(city)
    case _ => getPeople
  }

  /**
   * Get all the people of the entire application domain.
   *
   * @return  people of the entire application domain.
   */
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
