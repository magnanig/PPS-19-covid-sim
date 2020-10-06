package pps.covid_sim.util.scheduling

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.SortedSet
import scala.collection.immutable.{SortedMap, TreeMap, TreeSet}

case class Agenda(owner: Person) {

  private var plans: Set[Plan[_ <: Location]] = Set()

  private var fixedAppointments: SortedMap[Appointment, Group] = TreeMap()
  private var joinedAppointments: SortedSet[Appointment] = TreeSet()

  /**
   * Adds a new plan
   * @param plan  the plan to be added
   */
  def addPlan(plan: Plan[_ <: Location]): Unit = {
    plans += plan
  }

  /**
   * Removes the specified plan.
   * @param plan  the plan to be removed
   */
  def removePlan(plan: Plan[_ <: Location]): Unit = {
    plans -= plan
  }

  /**
   * Tries to fix the appointment for the current group, if agenda is free when requested.
   * Furthermore, if already exists such an appointment, merge old group with the current one.
   * @param appointment   the appointment to be fixed
   * @param group         the group participating at the appointment
   * @param merger        a function to merge an eventual already existing group, associated to
   *                      the appointment, with the current group
   * @return              true if the appointment has been fixed successfully, false otherwise
   */
  def fixAppointment(appointment: Appointment, group: Group)(implicit merger: Group => Group = _ + group): Boolean = {
    if(isFreeAt(appointment.datesInterval)) {
      fixedAppointments += (fixedAppointments.get(appointment) match {
        case Some(group: Group) => appointment -> merger(group)
        case _ => appointment -> group
      })
      true
    } else false
  }

  /**
   * Joins to the specified appointment, if agenda is free at that moment.
   * @param appointment   the appointment to which join in
   * @return              true if appointment has been registered successfully, false otherwise
   */
  def joinAppointment(appointment: Appointment): Boolean = {
    if (isFreeAt(appointment.datesInterval)){
      joinedAppointments += appointment
      true
    } else false
  }

  /**
   * Removes the specified appointment.
   * @param appointment   the appointment to be removed from agenda
   */
  def removeAppointment(appointment: Appointment): Unit = {
    fixedAppointments -= appointment
    joinedAppointments -= appointment
  }

  /**
   * Removes all appointment ended before the specified time.
   * @param time  the time before to which remove all ended appointments
   */
  def removeAppointmentsEndedBefore(time: Calendar): Unit = {
    fixedAppointments = fixedAppointments.filter(_._1.datesInterval.until > time)
    joinedAppointments = joinedAppointments.filter(_.datesInterval.until > time)
  }

  /**
   * Finds the first next free time interval, limited by the desired hours (default is 12).
   * @param startingFrom    the starting moment
   * @param limit           the max number of hours of the returned interval
   * @return                the first next free time interval
   */
  def firstNextFreeTime(startingFrom: Calendar)(implicit limit: Int = 12): DatesInterval = {
    val busyIntervals = fixedAppointments.filter(_._1.datesInterval.until > startingFrom).keySet
      .union(joinedAppointments.filter(_.datesInterval.until > startingFrom).keySet)
      .map(_.datesInterval)
      .toSeq.sorted
    // following can be optimized, if needed
    val interval = Iterator.iterate(startingFrom)(_ + 1)
      .dropWhile(time => plans.exists(_.isDefinedBetween(time)) || busyIntervals.exists(_.contains(time)))
      .takeWhile(time => plans.forall(!_.isDefinedBetween(time)) && busyIntervals.forall(!_.contains(time)))
      .take(limit)
      .toList
    DatesInterval(interval.head, interval.last + 1)
  }

  /**
   * Checks whether agenda is free between the specified dates interval.
   * @param datesInterval   the desired dates interval
   * @return                true if agenda is free between dates interval, false otherwise
   */
  def isFreeAt(datesInterval: DatesInterval): Boolean = !plans.exists(_.isDefinedBetween(datesInterval)) &&
    !fixedAppointments.exists(_._1.datesInterval.overlaps(datesInterval)) &&
    !joinedAppointments.exists(_.datesInterval.overlaps(datesInterval))

  /**
   * Check if agenda is busy at the specified time.
   * @param time    the desired time
   * @return        true if agenda is busy at time, false otherwise
   */
  def isBusyAt(time: Calendar): Boolean = !isFreeAt(time)

  /**
   * Get the fixed appointment that starts at the specified time.
   * @param time  the desired time
   * @return      the optional pair with the appointment and the group associated to it
   */
  def fixedAppointmentStartingAt(time: Calendar): Option[(Appointment, Group)] = fixedAppointments.headOption match {
    case Some((appointment, group)) if appointment.datesInterval.from is time => Some(appointment, group)
    case _ => None
  }

  /**
   * Get the joined appointment that starts at the specified time.
   * @param time  the desired time
   * @return      the optional appointment
   */
  def joinedAppointmentStartingAt(time: Calendar): Option[Appointment] = joinedAppointments.headOption match {
    case Some(appointment) if appointment.datesInterval.from is time => Some(appointment)
    case _ => None
  }

  /**
   * Get the plan that starts at the specified time.
   * @param time  the desired time
   * @return      an optional pair with the dates interval and the relative location
   */
  def planStartingAt(time: Calendar): Option[(DatesInterval, Location)] = plans.toStream
    .filter(p => {
      val enabled = p.isEnabled(time)
      enabled
    })
    .flatMap(_.dayPlan(time.day).toStream)
    .collectFirst({
      case (hours, location) if hours.from == time.hour => (DatesInterval(time, time + hours.size - 1), location)
    })

  /**
   * Get the next appointment, either fixed or joined, starting from the specified time.
   * @param time  the desired time
   * @return      an optional tuple with all information about the appointment, like the dates interval,
   *              the location and the optional group associated to the appointment
   * @note        the only case when group is empty, is when the commitment is a joined appointment.
   */
  def nextAppointment(time: Calendar): Option[(DatesInterval, Location, Option[Group])] = {
    fixedAppointmentStartingAt(time) match {
      case Some((appointment, group)) => Some(appointment.datesInterval, appointment.place, Some(group))
      case _ => joinedAppointmentStartingAt(time) match {
        case Some(appointment) => Some(appointment.datesInterval, appointment.place, None)
        case _ => None
      }
    }
  }

  /**
   * Get the next commitment starting at the specified time. It can be either a plan or an appointment.
   * @param time  the desired time
   * @return      an optional tuple with all information about the commitment, like the dates interval,
   *              the location and the optional group associated to the commitment.
   * @note        the only case when group is empty, is when the commitment is a joined appointment.
   */
  def nextCommitment(time: Calendar): Option[(DatesInterval, Location, Option[Group])] = {
    planStartingAt(time) match {
      case Some((datesInterval, location)) => /*println(s"Plan at ${location.getClass.getSimpleName}")*/
        Some((datesInterval, location, Some(owner)))
      case _ => nextAppointment(time)
    }
  }

}
