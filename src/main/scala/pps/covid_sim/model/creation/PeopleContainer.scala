package pps.covid_sim.model.creation

import pps.covid_sim.model.people.Person

object PeopleContainer {

  private var _people: List[Person] = List()

  def add(person: Person): Unit = { _people = person :: _people }

  def add(people: List[Person]): Unit = { _people = _people ::: people }

  def getPeople: List[Person] = _people

}
