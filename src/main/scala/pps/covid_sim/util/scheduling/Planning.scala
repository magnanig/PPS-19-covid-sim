package pps.covid_sim.util.scheduling

import pps.covid_sim.model.places.Education.Classroom
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.{Day, Month}
import pps.covid_sim.util.time.{DaysInterval, HoursInterval, MonthsInterval}

object Planning {

  private[Planning] abstract class PlanMap[T <: Location, A <: PlanMap[T, A]](val builder: () => A) extends Plan[T] {

    private var plan: Map[Day, DayPlan[T, A]] = Day.values.map(_ -> new DayPlan[T, A]()).toMap

    override type P = A

    private var editing: Day = _

    override def dayPlan(day: Day): DayPlan[T, P] = {
      editing = day
      plan(day).startEditing(this.asInstanceOf[P])
    }

    override def location(day: Day, hour: Int): Option[T] = plan(day).getLocation(hour)

    override def isDefinedOn(day: Day): Boolean = this.plan(day).nonEmpty

    override def isDefinedOn(day: Day, hourInterval: HoursInterval): Boolean = plan(day).isDefinedAt(hourInterval)

    override def add(location: T, day: Day, hoursInterval: HoursInterval): A = {
      if (hoursInterval.until > hoursInterval.from)
        build(this.plan + (day -> this.plan(day).add(location, hoursInterval)))
      else
        build(this.plan +
          (day ->
            this.plan(day).add(location, HoursInterval(hoursInterval.from, 0))) +
          (Day((day.id + 1) % Day.values.size) ->
            this.plan(Day((day.id + 1) % Day.values.size)).add(location, HoursInterval(0, hoursInterval.until)))
        )
    }

    override def add(location: T, day: Day, hoursIntervals: HoursInterval*): A = {
      build(this.plan + (day -> this.plan(day).add(location, hoursIntervals: _*)))
    }

    override def add(location: T, daysInterval: DaysInterval, hoursIntervals: HoursInterval*): A = {
      var current: A = this.asInstanceOf[A]
      daysInterval.foreach(day => current = current.add(location, day, hoursIntervals: _*))
      current
    }

    override def commit(): A = this.asInstanceOf[A]

    override def clear(day: Day): A = build(this.plan + (day -> new DayPlan[T, A]()))

    override private[scheduling] def commit[B <: Plan[T]](dayPlan: DayPlan[T, B]): B = build(this.plan +
      (editing -> dayPlan.asInstanceOf[DayPlan[T, A]])).asInstanceOf[B]

    private def build(values: Map[Day, DayPlan[T, A]]): A = {
      val p = builder()
      p.plan = values
      p
    }
  }


  class DayPlan[T <: Location, P <: Plan[T]] private[Planning](private val scheduling: Map[HoursInterval, T]
                                                                     = Map[HoursInterval, T](),
                                                                     private var editor: P = null.asInstanceOf[P])
    extends Iterable[(HoursInterval, T)] {

    /**
     * Get the location at the specified hour.
     * @param hour  the desired hour
     * @return      the optional location at the specified hour
     */
    def getLocation(hour: Int): Option[T] = scheduling.collectFirst({ case entry if entry._1.contains(hour) => entry._2 })

    /**
     * Check whether plan is defined at the specified hours interval.
     *
     * @param hoursInterval   the desired hours interval
     * @return                true if plan is defined at the specified hours interval, false otherwise
     */
    def isDefinedAt(hoursInterval: HoursInterval): Boolean = scheduling.keys.exists(_.overlaps(hoursInterval))

    /**
     * Schedules the specified location for the specified hours interval.
     *
     * @param location      the location to be added
     * @param hoursInterval the hours interval to which add location
     * @return              a copy of the current day plan updated as desired
     */
    def add(location: T, hoursInterval: HoursInterval): DayPlan[T, P] = new DayPlan(scheduling +
      (hoursInterval -> location), editor)

    /**
     * Schedules the specified location for the specified hours intervals.
     *
     * @param location        the location to be added
     * @param hoursIntervals  the hours interval to which add location
     * @return                a copy of the current day plan updated as desired
     */
    def add(location: T, hoursIntervals: HoursInterval*): DayPlan[T, P] = {
      var current = this
      hoursIntervals.foreach(hour => current = current.add(location, hour))
      current
    }

    /**
     * Ends the current day plan editing.
     * @return  the original plan containing this day plan
     */
    def commit(): P = editor.commit(this)

    /**
     * Clear all scheduling for the current day.
     * @return a new instance with an empty scheduling
     */
    def clear(): DayPlan[T, P] = new DayPlan[T, P](editor = this.editor)

    private[Planning] def startEditing(editor: P): DayPlan[T, P] = {
      this.editor = editor
      this
    }

    override def iterator: Iterator[(HoursInterval, T)] = scheduling.iterator
  }

  import pps.covid_sim.util.time.TimeIntervalsImplicits._

  case class StudentPlan(override val period: MonthsInterval = Month.SEPTEMBER -> Month.MAY)
    extends PlanMap[Classroom, StudentPlan](() => StudentPlan(period))

  case class WorkPlan[T <: Location](override val period: MonthsInterval = MonthsInterval.ALL_YEAR)
    extends PlanMap[T, WorkPlan[T]](() => WorkPlan(period))

  case class HobbyPlan[T <: Location](override val period: MonthsInterval = MonthsInterval.ALL_YEAR)
    extends PlanMap[T, HobbyPlan[T]](() => HobbyPlan(period))

  case class CustomPlan[T <: Location](override val period: MonthsInterval = MonthsInterval.ALL_YEAR)
    extends PlanMap[T, CustomPlan[T]](() => CustomPlan(period))

}
