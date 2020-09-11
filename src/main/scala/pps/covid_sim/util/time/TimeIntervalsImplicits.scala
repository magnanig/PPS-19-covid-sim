package pps.covid_sim.util.time

import java.util.Calendar

import pps.covid_sim.util.time.Time.Day.Day
import pps.covid_sim.util.time.Time.Month.Month

object TimeIntervalsImplicits {

  implicit class IntsToInterval(hour: Int) {
    def ->(until: Int): HoursInterval = HoursInterval(hour, until)
  }

  implicit class DaysToInterval(day: Day) {
    def ->(to: Day): DaysInterval = DaysInterval(day, to)
  }

  implicit class MonthsToInterval(month: Month) {
    def ->(to: Month): MonthsInterval = MonthsInterval(month, to)
  }

  implicit class DatesToInterval(date: Calendar) {
    def ->(until: Calendar): DatesInterval = DatesInterval(date, until)
  }

}
