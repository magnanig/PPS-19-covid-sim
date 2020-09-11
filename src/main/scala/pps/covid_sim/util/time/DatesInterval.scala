package pps.covid_sim.util.time

import java.util.Calendar

import pps.covid_sim.util.time.Time.ScalaCalendar

/**
 * <p>Represents a date interval, from an inclusive date to an exclusive one.</p>
 * <p><b>Example:</b> <pre>DateInterval(Calendar(2020-07-01:10), Calendar(2020-07-06:16))</pre>
 * represents an interval from 1st July 2020 at 10.00 to 6th July 2020 at 15:59.
 * Notice that "Calendar(2020-07-01:10)" is just pseudocode.</p>
 *
 * @param from  the starting inclusive date
 * @param until the final exclusive date
 */
case class DatesInterval(override val from: Calendar,
                         until: Calendar)
  extends Interval[Calendar](from, until - 1, _ + 1, identity) with Ordered[DatesInterval] {
  require(until > from)

  /**
   * Limits current dates interval by the specified amount of hours, starting from the beginning.
   * If the specified hours size is greater than current dates interval one, nothing happens.
   * @param hours   the length, in hours, of the returned dates interval, starting from the beginning
   *                of current one
   * @return        current dates interval, limited by hours
   */
  def limits(hours: Int): DatesInterval = DatesInterval(
    this.from,
    Seq(this.until, this.from + hours).min
  )

  override def compare(that: DatesInterval): Int = this.from.compareTo(that.from)

  override def toString(): String = "DatesInterval(from " + from.getTime + " to " + until.getTime + ")"
}

object DatesInterval {
  implicit def calendarToDatesInterval(calendar: Calendar): DatesInterval = DatesInterval(calendar, calendar + 1)
}