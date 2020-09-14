package pps.covid_sim.util.scheduling

import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.scheduling.Planning.DayPlan
import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.{DaysInterval, HoursInterval}

trait Plan[T <: Location] extends Schedule {

  type P <: Plan[T]

  /**
   * Get the plan for the specified day
   *
   * @param day the desired day
   * @return the plan for the specified day
   */
  def dayPlan(day: Day): DayPlan[T, P]

  /**
   * Get the location at the specified day and hour.
   *
   * @param day   the desired day
   * @param hour  the desired hour
   * @return      the optional location at the specified day and hour
   */
  def location(day: Day, hour: Int): Option[T]

  /**
   * Schedules the specified location for the specified day and hours interval.
   *
   * @param location      the location to be added
   * @param day           the day to which add location
   * @param hoursInterval the hours interval in the day to which add location
   * @return              a copy of the current plan updated as desired
   */
  def add(location: T, day: Day, hoursInterval: HoursInterval): P

  /**
   * Schedules the specified location for the specified day and hours intervals.
   *
   * @param location       the location to be added
   * @param day            the day to which add location
   * @param hoursIntervals the hours intervals in the day to which add location
   * @return a copy of the current plan updated as desired
   */
  def add(location: T, day: Day, hoursIntervals: HoursInterval*): P

  /**
   * Schedules the specified location for the specified days and hours intervals.
   *
   * @param location       the location to be added
   * @param daysInterval   the days to which add location
   * @param hoursIntervals the hours intervals in the days to which add location
   * @return a copy of the current plan updated as desired
   */
  def add(location: T, daysInterval: DaysInterval, hoursIntervals: HoursInterval*): P

  /**
   * Ends the editing.
   *
   * @return the current plan with all changes properly saved
   */
  def commit(): P

  private[scheduling] def commit[A <: Plan[T]](dayPlan: DayPlan[T, A]): A

}
