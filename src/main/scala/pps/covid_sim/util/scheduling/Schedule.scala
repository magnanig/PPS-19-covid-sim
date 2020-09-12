package pps.covid_sim.util.scheduling

import java.util.Calendar

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.{DatesInterval, HoursInterval, MonthsInterval}

/**
 * A scheduling that specify when some activity is defined.
 */
trait Schedule {

  /**
   * The months interval on which current scheduling is enabled.
   */
  protected val period: MonthsInterval

  /**
   * Check whether current scheduling is enabled at the specified time.
   *
   * @param time the desired time
   * @return true if current scheduling is enabled when desired, false otherwise
   */
  def isEnabled(time: Calendar): Boolean = period.contains(time.month)

  /**
   * Check whether some activity is scheduled at the specified day.
   *
   * @param day the desired day
   * @return true if some activity is scheduled when desired, false otherwise
   */
  def isDefinedOn(day: Day): Boolean

  /**
   * Check whether some activity is scheduled at the specified day and hours interval.
   *
   * @param day           the desired day
   * @param hoursInterval the desired hours interval
   * @return true if some activity is scheduled when desired, false otherwise
   */
  def isDefinedOn(day: Day, hoursInterval: HoursInterval): Boolean

  /**
   * Check whether some activity is scheduled at the specified time.
   *
   * @param time the desired time
   * @return true if some activity is scheduled when desired, false otherwise
   */
  def isDefinedAt(time: Calendar): Boolean = isDefinedOn(time.day, time.hour)

  /**
   * Check whether some activity is scheduled between the specified dates interval.
   *
   * @param datesInterval the dates interval to be tested
   * @return true if some activity is scheduled when desired, false otherwise
   */
  def isDefinedBetween(datesInterval: DatesInterval): Boolean = period.overlapsPeriod(datesInterval) &&
    datesInterval.exists(isDefinedAt)

  /**
   * Clear all scheduling for the specified day.
   *
   * @param day the day to be cleared
   * @return a new instance with an empty scheduling for day
   */
  def clear(day: Day): Schedule
}
