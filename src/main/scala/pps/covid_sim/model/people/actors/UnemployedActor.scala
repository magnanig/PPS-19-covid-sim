package pps.covid_sim.model.people.actors

import pps.covid_sim.model.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.parameters.GoingOutParameters
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.DaysInterval
import pps.covid_sim.util.time.TimeIntervalsImplicits._
import pps.covid_sim.util.time.Time.{Day, Month, Season}


class UnemployedActor() extends PersonActor {

  override protected lazy val maxGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
    .byMonthsInterval(Month.SEPTEMBER -> Month.MAY)
      .add(DaysInterval.WEEK, RandomGeneration.randomIntInRange(0,
        GoingOutParameters.maxGoingOutTimes(Season.WINTER, DaysInterval.WEEK, person.age)))
      .add(DaysInterval.WEEKEND, RandomGeneration.randomIntInRange(0,
        GoingOutParameters.maxGoingOutTimes(Season.WINTER, DaysInterval.WEEKEND, person.age)))
      .commit()
    .byMonthsInterval(Month.JUNE -> Month.AUGUST)
      .add(Day.MONDAY -> Day.SUNDAY, RandomGeneration.randomIntInRange(1,
        GoingOutParameters.maxGoingOutTimes(Season.SUMMER, DaysInterval.WEEK, person.age) +
          GoingOutParameters.maxGoingOutTimes(Season.SUMMER, DaysInterval.WEEKEND, person.age)))
      .commit()
}
