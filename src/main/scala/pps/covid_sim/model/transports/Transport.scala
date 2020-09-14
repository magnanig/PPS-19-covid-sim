package pps.covid_sim.model.transports

import pps.covid_sim.model.people.PeopleGroup.{Group, Single}

/**
 * A generic means of transport that people can take (can be either a public or private transport)
 */
trait Transport {

  val capacity: Int
  protected[Transport] var _numCurrentPeople: Int = 0
  protected[Transport] var _peopleSet: Set[Group] = Set()

  /**
   * Get the number of people inside the means of transport.
   * @return  the number of people inside the means of transport
   */
  def numCurrentPeople: Int = _numCurrentPeople

  /**
   * Lets the specified person enter to the means of transport at the specified time, if it is possible.
   * @param person  the person that wants to enter
   * @return        the optional means of transport (e.g. the bus) where person has been entered
   */
  def enter(person: Single): Option[Transport] = {
    if (_numCurrentPeople < capacity && !_peopleSet.contains(person)) {
      _numCurrentPeople += 1
      _peopleSet += person
      Some(this)
    } else {
      None
    }
  }

  /**
   * Lets person exit from the means of transport.
   * @param person   the person that wants to exit
   */
  def exit(person: Single): Unit = {
    if (capacity > 0 && _peopleSet.contains(person)) {
      _numCurrentPeople -= 1
      _peopleSet -= person
    }
  }

}
