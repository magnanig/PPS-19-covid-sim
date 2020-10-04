package pps.covid_sim.util.time

import java.util.Calendar

import pps.covid_sim.util.scheduling.TimeTable
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
  require(until >= from, s"Can't create DatesInterval(${from.getTime} -> ${until.getTime})") //TODO

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

  /**
   * Clips current dates interval at the begin and/or at the end, in such way that the
   * specified time table is defined for each time inside the obtained dates interval.
   * @param timeTable                 a time table enabled and defined at least one hour
   *                                  inside the current dates interval
   * @return                          a new dates interval, obtained from current one by
   *                                  clipping at the start and/or at the end, such that
   *                                  the time table is defined for each time inside the
   *                                  returned dates interval
   * @throws NoSuchElementException   if the specified timeTable is never defined inside
   *                                  the current interval
   */
  def clipToTimeTable(timeTable: TimeTable): DatesInterval = {
    val openingHours = timeTable.get(this).get // it is assumed that we are here only if we know timeTable is defined
    DatesInterval(openingHours.from, Seq(openingHours.until, this.until).min)
  }

  override def compare(that: DatesInterval): Int = this.from.compareTo(that.from)

  override def toString(): String = "DatesInterval(from " + from.getTime + " to " + until.getTime + ")"
}

object DatesInterval {
  implicit def calendarToDatesInterval(calendar: Calendar): DatesInterval = DatesInterval(calendar, calendar + 1)
}