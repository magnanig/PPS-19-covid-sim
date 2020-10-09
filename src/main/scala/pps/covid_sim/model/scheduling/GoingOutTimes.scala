package pps.covid_sim.model.scheduling

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.Month.Month
import pps.covid_sim.util.time.{DaysInterval, MonthsInterval}

object GoingOutTimes {

  trait GoingOutTimes {
    /**
     * Set the specified going out times for the desired moths interval, in a specific days interval.
     * @param monthsInterval  the desired months interval
     * @param daysInterval    the days interval only referring to above months interval
     * @param times           the going out times for the specified period
     * @return                a new instance, updated as requested
     */
    def add(monthsInterval: MonthsInterval, daysInterval: DaysInterval, times: Int): GoingOutTimes

    /**
     * Get the MonthsGoingOutTimes instance for the specified months interval.
     * @param monthsInterval  the desired months interval
     * @return                the MonthsGoingOutTimes instance for the specified months interval
     */
    def byMonthsInterval(monthsInterval: MonthsInterval): MonthsGoingOutTimes

    /**
     * Get the going out times for the specified month and day.
     * @param month   the desired month
     * @param day     the desired day
     * @return        an optional pair with the days interval containing day and the associated
     *                number of going out
     */
    def get(month: Month, day: Day): Option[(DaysInterval, Int)]

    /**
     * Get the probability of going out for the specified month and day.
     * @param month   the desired month
     * @param day     the desired day
     * @return        the probbaility of going out in day in month
     */
    def probability(month: Month, day: Day): Double

    /**
     * Remove the going out configuration for the specified months interval.
     * @param monthsInterval  the desired moths interval
     * @return                a new instance, updated as requested
     */
    def remove(monthsInterval: MonthsInterval): GoingOutTimes
  }

  case class GoingOutTimesMap(private val goingOutTimes: Map[MonthsInterval, MonthsGoingOutTimes] = Map())
    extends GoingOutTimes {

    private var editing: MonthsInterval = _

    override def add(monthsInterval: MonthsInterval, daysInterval: DaysInterval, times: Int): GoingOutTimes = {
      goingOutTimes.get(monthsInterval) match {
        case Some(monthsGoingOutTimes) => GoingOutTimesMap(
          goingOutTimes + (monthsInterval -> monthsGoingOutTimes.add(daysInterval, times))
        )
        case None => GoingOutTimesMap(
          goingOutTimes + (monthsInterval -> MonthsGoingOutTimes().add(daysInterval, times))
        )
      }
    }

    override def byMonthsInterval(monthsInterval: MonthsInterval): MonthsGoingOutTimes = {
      editing = monthsInterval
      goingOutTimes.get(monthsInterval) match {
        case Some(goingOutTimes) => goingOutTimes.editor = this; goingOutTimes
        case _ => MonthsGoingOutTimes(editor = this)
      }
    }

    override def get(month: Month, day: Day): Option[(DaysInterval, Int)] = goingOutTimes
      .collectFirst({ case e if e._1.contains(month) => e._2 }) match {
      case Some(monthsGoingOutTimes) => monthsGoingOutTimes.get(day)
      case _ => None
    }

    override def probability(month: Month, day: Day): Double = get(month, day) match {
      case Some((daysInterval, times)) => times / daysInterval.size.toDouble
      case _ => 0
    }

    override def remove(monthsInterval: MonthsInterval): GoingOutTimes = GoingOutTimesMap(goingOutTimes - monthsInterval)

    private[GoingOutTimes] def commit(monthGoingOutTimes: MonthsGoingOutTimes): GoingOutTimes = GoingOutTimesMap(
      goingOutTimes + (editing -> monthGoingOutTimes)
    )
  }

  private[GoingOutTimes] case class MonthsGoingOutTimes(private val monthGoingOutTimes: Map[DaysInterval, Int] = Map(),
                                 private[GoingOutTimes] var editor: GoingOutTimesMap = null) {

    def add(daysInterval: DaysInterval, times: Int): MonthsGoingOutTimes = MonthsGoingOutTimes(
      monthGoingOutTimes + (daysInterval -> times), editor
    )

    def get(day: Day): Option[(DaysInterval, Int)] = monthGoingOutTimes
      .collectFirst({ case e if e._1.contains(day) => e })

    def probability(day: Day): Double = get(day) match {
      case Some((daysInterval, num)) => num / daysInterval.size.toDouble
      case _ => 0
    }

    def commit(): GoingOutTimes = editor.commit(this)

    def clear(): MonthsGoingOutTimes = MonthsGoingOutTimes()
  }

}
