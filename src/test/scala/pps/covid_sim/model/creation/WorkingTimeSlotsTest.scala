package pps.covid_sim.model.creation

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.creation.institute.WorkingTimeSlots
import pps.covid_sim.model.places.Education.Classroom
import pps.covid_sim.util.time.Time.Day.Day

class WorkingTimeSlotsTest {

  val interval_1: Iterator[(Classroom, Day, Int)] = WorkingTimeSlots(List(Classroom(5))).iterator
  val interval_2: Iterator[(Classroom, Day, Int)] = WorkingTimeSlots(List(Classroom(5), Classroom(6))).iterator

  @Test
  def testSlotCreation(): Unit = {
    var count: Int = 0
    while (interval_1.hasNext) { interval_1.next; count += 1 }
    assertEquals(1 * 5 * 6, count)
    count = 0
    while (interval_2.hasNext) { interval_2.next; count += 1 }
    assertEquals(2 * 5 * 6, count)
  }

}