package pps.covid_sim.util.time

import java.util.Calendar

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.Month.Month

object Time {

  object Season extends Enumeration {
    type Season = Value
    val SPRING, SUMMER, AUTUMN, WINTER = Value
  }

  object Month extends Enumeration {
    type Month = Value
    val JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER = Value
  }

  object Day extends Enumeration {
    type Day = Value
    val MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY = Value
  }

  /**
   * An implicit class to work easily with java.pps.covid_sim.util.Calendar in Scala style.
   *
   * @param calendar the calendar object on which work at
   */
  implicit class ScalaCalendar(calendar: Calendar) {

    /**
     * Adds the specified hours to the current date.
     *
     * @param hours the amount of hours to be added (if negative, they will be subtracted as well)
     * @return the new instance of calendar with the result of operation
     */
    def +(hours: Int): Calendar = {
      val cal = calendar.clone().asInstanceOf[Calendar]
      cal.add(Calendar.HOUR_OF_DAY, hours)
      cal
    }

    /**
     * Adds the specified days to the current date.
     *
     * @param days the amount of days to be added (if negative, they will be subtracted as well)
     * @return the new instance of calendar with the result of operation
     */
    def ++(days: Int): Calendar = {
      val cal = calendar.clone().asInstanceOf[Calendar]
      cal.add(Calendar.DATE, days)
      cal
    }

    /**
     * Subtracts the specified hours to the current date.
     *
     * @param hours the amount of hours to be subtracted (if negative, they will be added as well)
     * @return the new instance of calendar with the result of operation
     */
    def -(hours: Int): Calendar = this + -hours

    /**
     * Get the difference, in years, between two dates.
     *
     * @param startDate the first date (i.e. the oldest) to be subtracted to the current one
     * @return the difference, in years, between current and startDate
     */
    def --(startDate: Calendar): Int = {
      var diff = calendar.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)
      if (startDate.get(Calendar.MONTH) > calendar.get(Calendar.MONTH) ||
        (startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
          startDate.get(Calendar.DATE) > calendar.get(Calendar.DATE))) {
        diff = diff - 1
      }
      diff
    }

    /**
     * Get the difference, in hours, between two dates.
     *
     * @param startDate the first date (i.e. the oldest) to be subtracted to the current one
     * @return the difference, in hours, between current and startDate
     */
    def -(startDate: Calendar): Int = Math.round(
      (calendar.getTimeInMillis - startDate.getTimeInMillis) / (1000 * 60 * 60.0)
    ).toInt

    /**
     * Get the difference, in days, between two dates.
     *
     * @param startDate the first date (i.e. the oldest) to be subtracted to the current one
     * @return the difference, in days, between current and startDate
     */
    def \(startDate: Calendar): Int = Math.round(
      (calendar.getTimeInMillis - startDate.getTimeInMillis) / (1000 * 60 * 60 * 24.0)
    ).toInt

    /**
     * Check if current date is before the specified one.
     *
     * @param other the second date to be tested
     * @return true if the current date is before the specified one, false otherwise
     */
    def <(other: Calendar): Boolean = calendar.before(other)

    /**
     * Check if current date is before or at the same time of the specified one.
     * Two dates are at the same time if day, month, year and hour are all equals; minutes and seconds
     * are ignored.
     *
     * @param other the second date to be tested
     * @return true if the current date is before or at the same of the specified one,
     *         false otherwise
     */
    def <=(other: Calendar): Boolean = calendar < other || calendar == other

    /**
     * Check if current date is after the specified one.
     *
     * @param other the second date to be tested
     * @return true if the current date is after the specified one, false otherwise
     */
    def >(other: Calendar): Boolean = other < calendar

    /**
     * Check if current date is after or at the same time of the specified one.
     * Two dates are at the same time if day, month, year and hour are all equals; minutes and seconds
     * are ignored.
     *
     * @param other the second date to be tested
     * @return true if the current date is after or at the same of the specified one,
     *         false otherwise
     */
    def >=(other: Calendar): Boolean = other <= calendar

    /**
     * Check if current date is at the same time of the specified one. Two dates are at the same time
     * if day, month, year and hour are all equals; minutes and seconds are ignored.
     *
     * @param other the second date to be tested
     * @return true if the current date is at the same of the specified one, false otherwise
     */
    def is(other: Calendar): Boolean = other.get(Calendar.DATE) == calendar.get(Calendar.DATE) &&
      other.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
      other.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
      other.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)

    val month: Month = Month(calendar.get(Calendar.MONTH))

    val day: Day = Day((calendar.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7)

    val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)

    val minutes: Int = calendar.get(Calendar.MINUTE)

    override def equals(obj: Any): Boolean = obj match {
      case other: Calendar => calendar is other
      case _ => false
    }

  }

  object ScalaCalendar {
    def apply(year: Int, month: Int, day: Int, hour: Int = 0): Calendar = {
      import java.util.Calendar
      val date = Calendar.getInstance
      date.set(year, month - 1, day, hour, 0, 0)
      date.set(Calendar.MILLISECOND, 0)
      date
    }
  }

}
