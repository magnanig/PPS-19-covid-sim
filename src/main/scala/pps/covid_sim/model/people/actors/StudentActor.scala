package pps.covid_sim.model.people.actors

import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.util.time.Time.{Day, Month}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class StudentActor() extends PersonActor {

  override protected lazy val maxGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
    .byMonthsInterval(Month.SEPTEMBER -> Month.MAY)
      .add(Day.MONDAY -> Day.FRIDAY, RandomGeneration.randomIntInRange(0, 4))
      .add(Day.SATURDAY -> Day.SUNDAY, RandomGeneration.randomIntInRange(0, 2))
      .commit()
    .byMonthsInterval(Month.JUNE -> Month.AUGUST)
      .add(Day.MONDAY -> Day.SUNDAY, RandomGeneration.randomIntInRange(1, 6))
      .commit()
}
