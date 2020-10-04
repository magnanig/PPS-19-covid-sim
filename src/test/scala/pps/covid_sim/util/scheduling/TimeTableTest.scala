package pps.covid_sim.util.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
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

  @Test
  def testGet(): Unit = {
    val timeTable: TimeTable = TimeTable()
      .add(Day.MONDAY, 10 -> 15)
      .add(Day.SATURDAY, 22 -> 4)
      .add(Day.TUESDAY, 22 -> 0)
    assertEquals(Some(ScalaCalendar(2020, 10, 3, 22) -> ScalaCalendar(2020, 10, 4, 3)),
      timeTable.get(ScalaCalendar(2020, 10, 3, 19) -> ScalaCalendar(2020, 10, 4, 3)))
    assertEquals(Some(ScalaCalendar(2020, 10, 3, 22) -> ScalaCalendar(2020, 10, 4, 4)),
      timeTable.get(ScalaCalendar(2020, 10, 3) -> ScalaCalendar(2020, 10, 4, 4)))
    assertEquals(Some(ScalaCalendar(2020, 10, 5, 11) -> ScalaCalendar(2020, 10, 5, 15)),
      timeTable.get(ScalaCalendar(2020, 10, 5, 11) -> ScalaCalendar(2020, 10, 5, 20)))
    assertEquals(Some(ScalaCalendar(2020, 10, 4) -> ScalaCalendar(2020, 10, 4, 1)),
      timeTable.get(ScalaCalendar(2020, 10, 4) -> ScalaCalendar(2020, 10, 4, 1)))
    assertEquals(Some(ScalaCalendar(2020, 10, 6, 23) -> ScalaCalendar(2020, 10, 7)),
      timeTable.get(ScalaCalendar(2020, 10, 6, 23) -> ScalaCalendar(2020, 10, 7)))
  }

  private def testAllCombinations(timeTable: TimeTable, daysInterval: DaysInterval,
                                  hoursIntervals: HoursInterval*): Unit = {
    for(day <- daysInterval; hour <- hoursIntervals.flatten) {
      assert(timeTable.isDefinedOn(day, hour))
    }
  }

}
