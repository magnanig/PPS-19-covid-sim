package pps.covid_sim.util.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.util.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.util.time.{DaysInterval, MonthsInterval}

class GoingOutTimesTest {

  val maxGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
    .add(MonthsInterval.SUMMER, DaysInterval.ALL_WEEK, 5)
    .byMonthsInterval(MonthsInterval.AUTUMN)
      .add(DaysInterval.WEEK, 2)
      .add(DaysInterval.WEEKEND, 1)
      .commit()
    .add(MonthsInterval.WINTER, DaysInterval.ALL_WEEK, 2)
    .add(MonthsInterval.SPRING, DaysInterval.ALL_WEEK, 3)

  @Test
  def testGoingOutTimes(): Unit = {
    assertGet(MonthsInterval.SUMMER, DaysInterval.ALL_WEEK, 5)
    assertGet(MonthsInterval.AUTUMN, DaysInterval.WEEK, 2)
    assertGet(MonthsInterval.AUTUMN, DaysInterval.WEEKEND, 1)
    assertGet(MonthsInterval.WINTER, DaysInterval.ALL_WEEK, 2)
    assertGet(MonthsInterval.SPRING, DaysInterval.ALL_WEEK, 3)
  }

  @Test
  def testGoingOutProbabilities(): Unit = {
    assertProbabilities(MonthsInterval.SUMMER, DaysInterval.ALL_WEEK, 5.0 / 7)
    assertProbabilities(MonthsInterval.AUTUMN, DaysInterval.WEEK, 2.0 / 5)
    assertProbabilities(MonthsInterval.AUTUMN, DaysInterval.WEEKEND, 1.0 / 2)
    assertProbabilities(MonthsInterval.WINTER, DaysInterval.ALL_WEEK, 2.0 / 7)
    assertProbabilities(MonthsInterval.SPRING, DaysInterval.ALL_WEEK, 3.0 / 7)
  }

  def testNotSetPeriod(): Unit = {
    implicit val partialGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
      .add(MonthsInterval.SUMMER, DaysInterval.WEEK, 3)
    assertGet(MonthsInterval.SUMMER, DaysInterval.WEEK, 3)
    assertGet(MonthsInterval.SUMMER, DaysInterval.WEEKEND, 0)
    assertGet(MonthsInterval.WINTER, DaysInterval.ALL_WEEK, 0)
    assertProbabilities(MonthsInterval.SUMMER, DaysInterval.WEEK, 3.0 / 5)
    assertProbabilities(MonthsInterval.SUMMER, DaysInterval.WEEKEND, 0)
    assertProbabilities(MonthsInterval.SPRING, DaysInterval.ALL_WEEK, 0)
  }

  private def assertGet(monthsInterval: MonthsInterval,
                        daysInterval: DaysInterval,
                        expectedTimes: Int)
                       (implicit goingOutTimes: GoingOutTimes = maxGoingOutTimes): Unit = {
    for(month <- monthsInterval; day <- daysInterval)
      assertEquals(s"Failed $monthsInterval, $daysInterval (expected times: $expectedTimes)",
        Some(daysInterval, expectedTimes), goingOutTimes.get(month, day))
  }

  private def assertProbabilities(monthsInterval: MonthsInterval,
                                  daysInterval: DaysInterval,
                                  expectedProbability: Double)
                                  (implicit goingOutTimes: GoingOutTimes = maxGoingOutTimes): Unit = {
    val tolerance = 0.001
    for(month <- monthsInterval; day <- daysInterval)
      assertEquals(s"Failed $monthsInterval, $daysInterval (expected times: $expectedProbability)",
        expectedProbability, goingOutTimes.probability(month, day), tolerance)
  }

}
