package pps.covid_sim.model.scheduling

import java.util.Calendar

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, HoursInterval, MonthsInterval}

/**
 * A time table for time scheduling. For example, it is useful to represent a market week
 * time tables (at which time it is opened of closed for each day of the week).
 *
 * @param period the period (months) on which current time table is enabled; if
 *               not specified, it is enabled all year
 */
case class TimeTable(period: MonthsInterval = MonthsInterval.ALL_YEAR,
                     private val timeTable: Map[Day, Seq[HoursInterval]] = Map()) extends Schedule {

  /**
   * Add the specified hours interval to the desired day.
   *
   * @param day           the day of the week to be edited
   * @param hoursInterval the hours interval to be added to day
   * @return a new TimeTable instance updated as desired
   */
  def add(day: Day, hoursInterval: HoursInterval): TimeTable = {
    if (hoursInterval.until == 0 || hoursInterval.until > hoursInterval.from)
      TimeTable(period, timeTable + (day -> (timeTable.getOrElse(day, Seq()) :+ hoursInterval)))
    else
      TimeTable(period, timeTable +
        (day ->
          (timeTable.getOrElse(day, Seq()) :+ HoursInterval(hoursInterval.from, 0))) +
        (Day((day.id + 1) % Day.values.size) ->
          (timeTable.getOrElse(Day((day.id + 1) % Day.values.size), Seq()) :+ HoursInterval(0, hoursInterval.until)))
      )
  }

  /**
   * Add the hour interval(s) to the specified day. Notice that is possible to add more hour
   * intervals for a day (eg. Monday 9-13 and 15-18).
   *
   * @param day            the day whose time slot(s) has/have to be added
   * @param hoursIntervals the hour interval(s) to be added to day
   * @return a new TimeTable instance updated as desired
   */
  def add(day: Day, hoursIntervals: HoursInterval*): TimeTable = hoursIntervals
    .foldLeft(this)((acc, hoursInterval) => acc.add(day, hoursInterval))

  /**
   * Works like other overload, but with the possibility to assign the specified hour
   * interval(s) to multiple days.
   *
   * @param daysInterval   the days whose time slot(s) has/have to be added
   * @param hoursIntervals the hour interval(s) to be assigned
   * @return a new TimeTable instance updated as desired
   */
  def add(daysInterval: DaysInterval, hoursIntervals: HoursInterval*): TimeTable = daysInterval
    .foldLeft(this)((acc, day) => acc.add(day, hoursIntervals: _*))

  import pps.covid_sim.util.time.TimeIntervalsImplicits._
  /**
   * Get the dates interval, contained in the specified dates interval, on which time table is defined.
   *
   * @param datesInterval the dates interval on which looking for a sub-dates interval such that
   *                      time table is ever defined
   * @return the optional dates interval, contained in the specified one, respecting
   *         the above constraint
   */
  def get(datesInterval: DatesInterval): Option[DatesInterval] = {
    def _get(datesInterval: DatesInterval): Option[DatesInterval] = {
      datesInterval.find(isDefinedAt) match {
        case Some(time) => timeTable(time.day)
          .find(_.contains(time.hour))
          .map(hours => DatesInterval(time, Seq(time + HoursInterval(time.hour, hours.until).size,
            datesInterval.until).min))
        case _ => None
      }
    }

    _get(datesInterval) match {
      case Some(firstPeriod) if firstPeriod.until.hour == 0 && isDefinedAt(firstPeriod.until) =>
        _get(firstPeriod.until -> datesInterval.until) match {
          case Some(secondPeriod) => Some(DatesInterval(firstPeriod.from, secondPeriod.until))
          case None => Some(firstPeriod)
        }
      case datesInterval @ Some(_) => datesInterval
      case _ => None
    }
  }

  /**
   * Get time table for a specified day.
   *
   * @param day the desired day
   * @return the time table for the specified day, as sequence of hour intervals
   */
  def getDayTimeTable(day: Day): Seq[HoursInterval] = timeTable.getOrElse(day, Seq.empty)

  override def isDefinedOn(day: Day): Boolean = timeTable.get(day).exists(_.nonEmpty)

  override def isDefinedOn(day: Day, hoursInterval: HoursInterval): Boolean = timeTable.get(day)
    .exists(_.exists(_.overlaps(hoursInterval)))

  /**
   * Check whether current place is opened at specified time.
   * @param time the current time
   * @return true if place is open when desired, false otherwise
   */
  override def isDefinedAt(time: Calendar): Boolean = timeTable.get(time.day) match {
    case Some(hourIntervals) => hourIntervals.count(_.contains(time.hour)) > 0
    case _ => false
  }

  override def clear(day: Day): TimeTable = TimeTable(period, timeTable - day)

  override def toString: String = Day.values.map(day => day + ": " + (getDayTimeTable(day) match {
    case Seq(hoursIntervals) => hoursIntervals.toString()
    case _ => "Not open"
  })).mkString("\n")

}
