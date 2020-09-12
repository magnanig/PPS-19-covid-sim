package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.Day.Day

object Locations {

  trait Location {
    private var _currentGroups: Set[Group] = Set()
    private[Locations] var _numCurrentPeople: Int = 0

    /**
     * Get the number of people inside the current location.
     * @return  the number of people inside the current location
     */
    def numCurrentPeople: Int = _numCurrentPeople

    /**
     * Get the groups that have entered into current location.
     * @return  the set of group inside the current location
     */
    def currentGroups: Set[Group] = _currentGroups

    /**
     * Lets the specified group enter to current location at the specified time, if it is possible.
     * @param group   the group that wants to enter
     * @param time    the desired time
     * @return        the optional location (e.g. the room) where group has been entered
     */
    def enter(group: Group, time: Calendar): Option[Location]

    /**
     * Lets group exit from current location.
     * @param group   the group that wants to exit
     */
    def exit(group: Group): Unit
  }

  trait LimitedPeopleLocation extends Location {

    /**
     * The maximum location's capacity, in term of people that can be contained at the same time.
     */
    val capacity: Int

  }

}
