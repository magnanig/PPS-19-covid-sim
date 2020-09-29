package pps.covid_sim.model.container

import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person

object PeopleContainer {

  private var _people: List[Person] = List()

  def add(person: Person): Unit = {
    _people = person :: _people
  }

  def add(people: List[Person]): Unit = {
    _people = _people ::: people
  }

  def getPeople: List[Person] = _people

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
