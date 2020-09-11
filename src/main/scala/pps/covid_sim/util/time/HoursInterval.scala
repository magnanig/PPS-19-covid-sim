package pps.covid_sim.util.time

import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.TimeIntervalsImplicits._

/**
 * <p>Represents an hour interval, from an inclusive hour to an exclusive one, in 24 hour format,
 * i.e. from 0 to 23 both inclusive.</p>
 * <p><b>Example 1:</b> <code>HoursInterval(10, 12)</code> represents an interval from 10.00 to 11.59.
 * Thus, an iterator here would return 10 and 11, without 12.</p>
 * <p><b>Example 2:</b> <code>HoursInterval(23, 2)</code> represents an interval from 23.00 to 1.59.
 * Thus, an iterator here would return 23, 0, 1, and 2, without 3.</p>
 * @param from    the starting inclusive hour
 * @param until   the final exclusive hour
 */
case class HoursInterval(override val from: Int,
                         until: Int)
  extends CyclicInterval[Int](from, (until + 23) % 24, identity, h => (h + 1) % 24, d => d.from.hour -> d.until.hour){
  override protected val cycleLength: Int = 24

  override def toString(): String = "HoursInterval(from " + from + ":00 to " + until + ":00)"
}

object HoursInterval {
  implicit def intToHourInterval(value: Int): HoursInterval = HoursInterval(value, value + 1)
}