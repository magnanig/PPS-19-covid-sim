package pps.covid_sim.util.time

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

/**
 * <p>Represents a day interval, from an inclusive day to an inclusive one.</p>
 * <p><b>Example:</b> <code>DaysInterval(MONDAY, FRIDAY)</code> represents an interval from
 * Monday to Friday, both inclusive. Thus, an iterator here would return MONDAY, TUESDAY,
 * WEDNESDAY, THURSDAY, FRIDAY.</p>
 * @param from  the starting inclusive day
 * @param to    the final inclusive day
 */
case class DaysInterval(override val from: Day,
                        override val to: Day)
  extends CyclicInterval[Day](from, to, _.id, day => Day((day.id + 1) % Day.values.size),
    d => d.from.day -> d.until.day) {

  override def toString(): String = "DaysInterval(from " + from + " to " + to + ")"

  override protected val cycleLength: Int = Day.values.size
}

object DaysInterval {
  val WEEKEND: DaysInterval = DaysInterval(Day.SATURDAY, Day.SUNDAY)
  val WEEK: DaysInterval = DaysInterval(Day.MONDAY, Day.FRIDAY)
  val ALL_WEEK: DaysInterval = DaysInterval(Day.MONDAY, Day.SUNDAY)
}