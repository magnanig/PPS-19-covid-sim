package pps.covid_sim.util.time

import java.util.Calendar

import org.junit.Assert.{assertEquals, assertFalse}
import org.junit.Test
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
}
