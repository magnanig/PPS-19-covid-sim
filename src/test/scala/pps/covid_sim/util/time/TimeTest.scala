package pps.covid_sim.util.time

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, Month, ScalaCalendar}

class TimeTest {

  @Test
  def testCalendarOperation(): Unit = {
    val startingDate = ScalaCalendar(2020, 1, 1, 15)
    assert(ScalaCalendar(2020, 1, 1, 16) is startingDate + 1)
    assert(ScalaCalendar(2020, 1, 1, 14) is startingDate - 1)
    assertEquals(ScalaCalendar(2020, 1, 1), ScalaCalendar(2020, 1, 1))
    assert(ScalaCalendar(2020, 1, 1, 14) < ScalaCalendar(2020, 1, 1, 15))
    assertFalse(ScalaCalendar(2020, 1, 2, 14) < ScalaCalendar(2020, 1, 1, 15))
    assertFalse(ScalaCalendar(2020, 1, 1, 16) < ScalaCalendar(2020, 1, 1, 15))
    assert(ScalaCalendar(2020, 1, 1, 14) <= ScalaCalendar(2020, 1, 1, 14))
    assertFalse(ScalaCalendar(2020, 1, 1, 14) <= ScalaCalendar(2020, 1, 1, 13))
    assert(ScalaCalendar(2020, 1, 1, 10) > ScalaCalendar(2020, 1, 1, 9))
    assert(ScalaCalendar(2020, 1, 2, 10) > ScalaCalendar(2020, 1, 1, 15))
    assertFalse(ScalaCalendar(2020, 1, 1) > ScalaCalendar(2020, 1, 1))
    assert(ScalaCalendar(2020, 1, 1, 14) >= ScalaCalendar(2020, 1, 1, 13))
    assertFalse(ScalaCalendar(2020, 1, 1, 14) >= ScalaCalendar(2020, 1, 1, 15))
  }

  @Test
  def testCalendarConversions(): Unit = {
    assertEquals(Day.TUESDAY, ScalaCalendar(2019, 12, 31).day)
    assertEquals(Day.WEDNESDAY, ScalaCalendar(2020, 1, 1).day)
    assertEquals(Day.THURSDAY, ScalaCalendar(2020, 1, 2).day)
    assertEquals(Day.TUESDAY, ScalaCalendar(2020, 8, 4).day)
    assertEquals(10, ScalaCalendar(2019, 12, 31, 10).hour)
    assertEquals(12, ScalaCalendar(2020, 1, 1, 12).hour)
    assertEquals(5, ScalaCalendar(2020, 1, 2, 5).hour)
    assertEquals(23, ScalaCalendar(2020, 8, 4, 23).hour)
    assertEquals(Month.JANUARY, ScalaCalendar(2020, 1, 2, 5).month)
    assertEquals(Month.MARCH, ScalaCalendar(2020, 3, 30).month)
    assertEquals(Month.AUGUST, ScalaCalendar(2020, 8, 4, 23).month)
  }

  @Test
  def testHoursInterval(): Unit = {
    val hoursInterval = HoursInterval(10, 12)
    assert(!hoursInterval.contains(9))
    assert(hoursInterval.contains(10))
    assert(hoursInterval.contains(11))
    assert(!hoursInterval.contains(12))

    assert(hoursInterval.overlaps(HoursInterval(11, 15)))
    assert(!hoursInterval.overlaps(HoursInterval(12, 15)))
    assert(!hoursInterval.overlaps(HoursInterval(6, 10)))
    assert(!hoursInterval.overlaps(HoursInterval(8, 9)))
    assert(hoursInterval.overlaps(HoursInterval(10, 11)))
    assert(hoursInterval.overlaps(HoursInterval(23, 12)))
  }

  @Test
  def testCyclicHoursInterval(): Unit = {
    val hoursInterval = HoursInterval(23, 2)
    assert(!hoursInterval.contains(22))
    assert(hoursInterval.contains(23))
    assert(hoursInterval.contains(0))
    assert(hoursInterval.contains(1))
    assert(!hoursInterval.contains(2))

    assert(hoursInterval.overlaps(HoursInterval(21, 0)))
    assert(hoursInterval.overlaps(HoursInterval(10, 0)))
    assert(hoursInterval.overlaps(HoursInterval(0, 5)))
    assert(!hoursInterval.overlaps(HoursInterval(2, 23)))
    assert(!hoursInterval.overlaps(HoursInterval(20, 23)))
  }

  @Test
  def testDaysInterval(): Unit = {
    val daysInterval = DaysInterval(Day.SUNDAY, Day.WEDNESDAY)

    assert(daysInterval.contains(Day.SUNDAY))
    assert(daysInterval.contains(Day.MONDAY))
    assert(daysInterval.contains(Day.TUESDAY))
    assert(daysInterval.contains(Day.WEDNESDAY))
    assert(!daysInterval.contains(Day.THURSDAY))
    assert(!daysInterval.contains(Day.FRIDAY))
    assert(!daysInterval.contains(Day.SATURDAY))

    assert(daysInterval.overlaps(DaysInterval(Day.SATURDAY, Day.MONDAY)))
    assert(daysInterval.overlaps(DaysInterval(Day.WEDNESDAY, Day.FRIDAY)))
    assert(!daysInterval.overlaps(DaysInterval(Day.THURSDAY, Day.FRIDAY)))
    assert(daysInterval.overlaps(DaysInterval.WEEKEND))
    assert(daysInterval.overlaps(DaysInterval.ALL_WEEK))
  }

  @Test
  def testMonthsInterval(): Unit = {
    val monthsInterval = MonthsInterval(Month.DECEMBER, Month.FEBRUARY)
    assert(monthsInterval.contains(Month.DECEMBER))
    assert(monthsInterval.contains(Month.JANUARY))
    assert(monthsInterval.contains(Month.FEBRUARY))

    assert(monthsInterval.overlaps(MonthsInterval(Month.DECEMBER, Month.JANUARY)))
    assert(monthsInterval.overlaps(MonthsInterval(Month.FEBRUARY, Month.APRIL)))
    assert(!monthsInterval.overlaps(MonthsInterval(Month.MARCH, Month.MAY)))
  }

  @Test
  def testDatesInterval(): Unit = {
    import TimeIntervalsImplicits._
    val from = ScalaCalendar(2020, 10, 1)
    val until = ScalaCalendar(2020, 10, 5)
    val datesInterval = from -> until // creating DatesInterval from implicits
    assert(datesInterval.contains(ScalaCalendar(2020, 10, 1)))
    assert(datesInterval.contains(ScalaCalendar(2020, 10, 1, 1)))
    assert(datesInterval.contains(ScalaCalendar(2020, 10, 2)))
    assert(datesInterval.contains(ScalaCalendar(2020, 10, 4)))
    assert(datesInterval.contains(ScalaCalendar(2020, 10, 4, 23)))
    assert(!datesInterval.contains(ScalaCalendar(2020, 10, 5)))
    assert(!datesInterval.contains(ScalaCalendar(2020, 10, 6)))
    assertEquals(datesInterval.limits(3), from -> ScalaCalendar(2020, 10, 1, 3))
    assertEquals(datesInterval.limits(1), from -> ScalaCalendar(2020, 10, 1, 1))
    assertEquals(datesInterval.limits(500), from -> until) // if limits is greater than intervals, does nothing
  }

  @Test
  def testHourIntervalIterator(): Unit = {
    assertEquals(List(1, 2, 3), HoursInterval(1, 4).toList)
    assertEquals(List(15), HoursInterval(15, 16).toList)
  }

  @Test
  def testDayIntervalIterator(): Unit = {
    assertEquals(List(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY), DaysInterval(Day.MONDAY, Day.WEDNESDAY).toList)
    assertEquals(List(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY),
      DaysInterval(Day.MONDAY, Day.SUNDAY).toList)
    assertEquals(List(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY),
      DaysInterval.ALL_WEEK.toList)
    assertEquals(List(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY), DaysInterval.WEEK.toList)
    assertEquals(List(Day.SATURDAY, Day.SUNDAY), DaysInterval.WEEKEND.toList)
    assertEquals(List(Day.FRIDAY, Day.SATURDAY, Day.SUNDAY, Day.MONDAY), DaysInterval(Day.FRIDAY, Day.MONDAY).toList)
  }

  @Test
  def testMonthsIntervalIterator(): Unit = {
    assertEquals(Seq(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY), MonthsInterval(Month.DECEMBER, Month.FEBRUARY).toSeq)
  }

  @Test
  def testDateIntervalIterator(): Unit = {
    val from = ScalaCalendar(2020, 1, 1, 15)
    val until = ScalaCalendar(2020, 1, 5)
    val dateInterval = DatesInterval(from, until)
    val dateIntervalIterator = dateInterval.iterator
    var current: Calendar = null
    while(dateIntervalIterator.hasNext) {
      current = dateIntervalIterator.next()
      assert(current >= from && current < until)
    }
    assert(current == until - 1)
  }

  @Test
  def testClipToTimeTable(): Unit = {
    import TimeIntervalsImplicits._

    val timeTable = TimeTable()
      .add(Day.MONDAY, 8 -> 12, 15 -> 18, 20 -> 22)
      .add(Day.SATURDAY, 22 -> 4)

    var datesInterval = ScalaCalendar(2020, 9, 14) -> ScalaCalendar(2020, 9, 15) // Mon 14 Sep
    assertEquals(ScalaCalendar(2020, 9, 14, 8) -> ScalaCalendar(2020, 9, 14, 12),
      datesInterval.clipToTimeTable(timeTable))

    datesInterval = ScalaCalendar(2020, 9, 14, 16) -> ScalaCalendar(2020, 9, 15) // Mon 14 Sep after 16.00
    assertEquals(ScalaCalendar(2020, 9, 14, 16) -> ScalaCalendar(2020, 9, 14, 18),
      datesInterval.clipToTimeTable(timeTable))

    datesInterval = ScalaCalendar(2020, 9, 19, 23) -> ScalaCalendar(2020, 9, 20, 6) // Sat 19 Sep after 16.00
    assertEquals(ScalaCalendar(2020, 9, 19, 23) -> ScalaCalendar(2020, 9, 20, 4),
      datesInterval.clipToTimeTable(timeTable))

  }

}
