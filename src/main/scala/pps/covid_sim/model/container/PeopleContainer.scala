package pps.covid_sim.model.container

import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}

import scala.collection.parallel.ParSeq

object PeopleContainer {

  private var _people: ParSeq[Person] = ParSeq()

  /**
   * Delete all people created so far.
   */
  def reset(): Unit = {
    _people = ParSeq()
  }

  /**
   * Adds a person to the container.
   */
  def add(person: Person): Unit = {
    _people = person +: _people
  }

  /**
   * Adds a list of people to the container.
   */
  def add(people: List[Person]): Unit = {
    _people = _people ++ people.par
  }

  /**
   * Get all the people who live in a specific area.
   *
   * @param area  area where the required people live.
   * @return      people who live in a specific area.
   */
  def people(area: Area): ParSeq[Person] = area match {
    case province: Province => getPeople(province)
    case region: Region => getPeople(region)
    case city: City => getPeople(city)
    case _ => people
  }

  /**
   * Get all the people of the entire application domain.
   *
   * @return  people of the entire application domain.
   */
  def people: ParSeq[Person] = _people

  private def getPeople(city: City): ParSeq[Person] = people.filter(p => p.residence.equals(city))

  private def getPeople(province: Province): ParSeq[Person] =
    people.filter(p => p.residence.province.equals(province))

  private def getPeople(region: Region): ParSeq[Person] =
    people.filter(p => p.residence.province.region.equals(region))

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
