package pps.covid_sim.util.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._
import pps.covid_sim.util.time.{DaysInterval, HoursInterval}

class TimeTableTest {

  val timeTable: TimeTable = TimeTable()
    .add(Day.MONDAY, 8 -> 12)
    .add(Day.WEDNESDAY -> Day.FRIDAY, 8 -> 12, 14 -> 18)
    .add(DaysInterval.WEEKEND, 9 -> 20)

  @Test
  def testTimeTable(): Unit = {
    assert((Day.WEDNESDAY -> Day.MONDAY).forall(timeTable.isDefinedOn))
    assert(!timeTable.isDefinedOn(Day.MONDAY, 7 -> 8))
    assert(timeTable.isDefinedOn(Day.MONDAY, 8 -> 9))
    assert(timeTable.isDefinedOn(Day.MONDAY, 11 -> 12))
    assert(!timeTable.isDefinedOn(Day.MONDAY, 12 -> 13))
    assert(!timeTable.isDefinedOn(Day.TUESDAY))
    assertEquals(Seq(8 -> 12), timeTable.getDayTimeTable(Day.MONDAY))
    assertEquals(Seq.empty, timeTable.getDayTimeTable(Day.TUESDAY))
    (Day.WEDNESDAY -> Day.FRIDAY).foreach(day => assertEquals(Seq(8 -> 12, 14 -> 18), timeTable.getDayTimeTable(day)))
    DaysInterval.WEEKEND.foreach(day => assertEquals(Seq(9 -> 20), timeTable.getDayTimeTable(day)))
    (8 -> 12).foreach(h => assert(timeTable.isDefinedOn(Day.MONDAY, h)))
    testAllCombinations(timeTable, Day.WEDNESDAY -> Day.FRIDAY, 8 -> 12, 14 -> 18)
    testAllCombinations(timeTable, DaysInterval.WEEKEND, 9 -> 20)
  }

  @Test
  def testCyclicInterval(): Unit = {
    val timeTable = TimeTable()
      .add(Day.SATURDAY, 22 -> 4)
    assert(timeTable.isDefinedOn(Day.SATURDAY))
    assert(timeTable.isDefinedOn(Day.SUNDAY))
    assert(timeTable.isDefinedOn(Day.SATURDAY, 20 -> 23))
    assert(timeTable.isDefinedOn(Day.SATURDAY, 20 -> 0))
    assert(timeTable.isDefinedOn(Day.SUNDAY, 0 -> 4))
    assert(!timeTable.isDefinedOn(Day.SATURDAY, 0 -> 22))
    assert(!timeTable.isDefinedOn(Day.SUNDAY, 4 -> 0))
  }

  private def testAllCombinations(timeTable: TimeTable, daysInterval: DaysInterval,
                                  hoursIntervals: HoursInterval*): Unit = {
    for(day <- daysInterval; hour <- hoursIntervals.flatten) {
      assert(timeTable.isDefinedOn(day, hour))
    }
  }

}
