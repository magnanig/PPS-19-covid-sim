package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.clinical.VirusPropagation
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
    def currentGroups: Set[Group] = synchronized {
      _currentGroups
    }

    /**
     * Check whether current location is open in the specified day or not.
     * @param day   the desired day
     * @return      true if location is open at day, false otherwise
     */
    def isOpen(day: Day): Boolean = true

    /**
     * Check whether current location is open between the specified dates interval or not.
     * @param datesInterval   the desired dates interval
     * @return                true if location is open between the dates interval, false otherwise
     */
    def isOpen(datesInterval: DatesInterval): Boolean = true


    /**
     * Lets the specified group enter to current location at the specified time, if it is possible.
     * @param group   the group that wants to enter
     * @param time    the desired time
     * @return        the optional location (e.g. the room) where group has been entered
     */
    final def enter(group: Group, time: Calendar): Option[Location] = synchronized {
      if (_currentGroups.contains(group)) {
        println(s"WARNING: ${group.leader} Already entered in the ${getClass.getSimpleName}!")
        Some(this)
      } else if(canEnter(group, time)) preEnter(group, time) match {
        case location @ Some(_) => onEntered(group); location
        case _ => println(s"WARNING: $group cannot enter to the ${getClass.getSimpleName} at ${time.getTime}"); None
      } else {
        None
      }
    }

    /**
     * This method can be overridden to do all operations and/or controls in order to check
     * permission for group to enter in current location and set other information.
     * The default implementation doesn't do nothing.
     * @param group   the group that wants to enter
     * @param time    the desired time
     * @return        the optional location (e.g. the room) where group has been entered
     */
    protected def preEnter(group: Group, time: Calendar): Option[Location] = Some(this)

    /**
     * This method gets called after a group has been allowed to enter in current location.
     * The default implementation, adds group to the set of group entered in location and
     * updates the number of people as well. So, if you want to override this method, remember
     * to call "super" before.
     * @param group   the group that has been entered
     */
    protected def onEntered(group: Group): Unit = {
      _currentGroups += group
      _numCurrentPeople = _numCurrentPeople + group.size
    }

    /**
     * Lets group exit from current location.
     * @param group   the group that wants to exit
     */
    final def exit(group: Group): Unit = synchronized {
      if(currentGroups.contains(group)) {
        preExit(group)
        //println(s"${group.leader} Exited from ${getClass.getSimpleName}!")
        _currentGroups -= group
        _numCurrentPeople = _numCurrentPeople - group.size
      }
    }

    /**
     * In this method it is possible to specify all operations that must be done before
     * group exits from location. Default implementation is empty.
     * @param group   the group that is going to exit
     */
    protected def preExit(group: Group): Unit = {}

    /**
     * Propagates virus inside the current location. The default implementation propagates the
     * virus inside each group, trying to infect every pair of people where neither of them
     * is keeping safety distance from the other. Furthermore, each person looks for his/her friends
     * and, if present, may infect them as well. Subclasses can override this method as needed.
     * @param time  current time
     * @param place current place
     */
    def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = synchronized {
      _currentGroups
        .foreach(group => {
          if (group.size > 1) inGroupVirusPropagation(group, place, time)(covidInfectionParameters)
          lookForFriends(group, place, time)(covidInfectionParameters)
        })
    }

    /**
     * Propagates virus inside a group, trying to infect each pair of people.
     * @param group   the group inside to which propagate virus
     * @param place   the place where virus propagation takes place
     * @param time    the infection time
     */
    protected def inGroupVirusPropagation(group: Group,
                                          place: Location,
                                          time: Calendar)
                                         (covidInfectionParameters: CovidInfectionParameters): Unit = synchronized {
      group.toList
        .combinations(2)
        .foreach(pair => VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time))
    }

    /**
     * Looks for friends of each person inside the specified group and try to infect he or she
     * with each friend that is in current location too.
     * @param group   the group whose members look for their friend
     * @param place   the place where virus propagation takes place
     * @param time    the infection time
     */
    protected def lookForFriends(group: Group,
                                 place: Location,
                                 time: Calendar)
                                (covidInfectionParameters: CovidInfectionParameters): Unit = synchronized {
      group
        .foreach(person => person.friends
          .intersect((_currentGroups - group).flatMap(_.people))
          .foreach(VirusPropagation(covidInfectionParameters).tryInfect(person, _, place, time)))
    }

    /**
     * Looks for friends of each person of each group and try to infect each person with
     * each friend that is in current location too.
     * @param place   the place where virus propagation takes place
     * @param time    the infection time
     */
    protected def lookForFriends(place: Location,
                                 time: Calendar)
                                (covidInfectionParameters: CovidInfectionParameters): Unit = synchronized {
      _currentGroups.foreach(lookForFriends(_, place, time)(covidInfectionParameters))
    }

    /**
     * Checks whether group can enter to current location at the specified time.
     * @param group the group that wants to enter
     * @param time    the desired time
     * @return        true if group can enter to location at time, false otherwise
     */
    protected[places] def canEnter(group: Group, time: Calendar): Boolean = true

    /**
     * The optional needed mask in current location.
     * @return  the optional needed mask
     */
    def mask: Option[Mask] = None

    /**
     * Clear current location, removing all groups.
     */
    def clear(): Unit = synchronized {
      _numCurrentPeople = 0
      _currentGroups = Set()
    }

  }

  trait LimitedPeopleLocation extends Location {

    /**
     * The maximum location's capacity, in term of people that can be contained at the same time.
     */
    val capacity: Int

    override protected[places] def canEnter(group: Group, time: Calendar): Boolean = capacity - _numCurrentPeople >= group.size

    override protected def preEnter(group: Group, time: Calendar): Option[LimitedPeopleLocation] = {
      if(canEnter(group, time)) Some(this) else None
    }

  }

}
