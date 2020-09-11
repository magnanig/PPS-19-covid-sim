package pps.covid_sim.util.time

import pps.covid_sim.util.time.Time.Month.Month
import pps.covid_sim.util.time.Time.Season.Season
import pps.covid_sim.util.time.Time.{Month, ScalaCalendar, Season}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

/**
 * <p>Represents a months interval, from an inclusive month to an inclusive one.</p>
 * <p><b>Example:</b> <code>MonthsInterval(JANUARY, MARCH)</code> represents an interval
 * from January to March, both inclusive. Thus, an iterator here would return JANUARY, FEBRUARY, MARCH.</p>
 * @param from  the starting inclusive month
 * @param to    the final inclusive month
 */
case class MonthsInterval(override val from: Month,
                          override val to: Month)
  extends CyclicInterval[Month](from, to, _.id, month => Month((month.id + 1) % Month.values.size),
    d => d.from.month -> d.until.month) {

  def toSeason: Season = Set((MonthsInterval.SPRING, Season.SPRING), (MonthsInterval.SUMMER, Season.SUMMER),
    (MonthsInterval.AUTUMN, Season.AUTUMN), (MonthsInterval.WINTER, Season.WINTER))
    .map(months => (months._2, months._1.toList.intersect(this.toList).size))
    .max((x: (Season, Int), y: (Season, Int)) => x._2.compareTo(y._2))._1

  override def toString(): String = "pps.covid_sim.util.time.MonthsInterval(from " + from + " to " + to + ")"

  override protected val cycleLength: Int = Month.values.size
}

object MonthsInterval {
  val SPRING: MonthsInterval = MonthsInterval(Month.MARCH, Month.MAY)
  val SUMMER: MonthsInterval = MonthsInterval(Month.JUNE, Month.AUGUST)
  val AUTUMN: MonthsInterval = MonthsInterval(Month.SEPTEMBER, Month.NOVEMBER)
  val WINTER: MonthsInterval = MonthsInterval(Month.DECEMBER, Month.FEBRUARY)
  val ALL_YEAR: MonthsInterval = MonthsInterval(Month.JANUARY, Month.DECEMBER)
}