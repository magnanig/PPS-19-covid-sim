package pps.covid_sim.model.creation

import org.junit.Test
import org.junit.Assert.assertEquals
import pps.covid_sim.model.creation.institute.WorkingTimeSlots
import pps.covid_sim.model.places.Education.Classroom

class WorkingTimeSlotsTest {

  val interval_1: WorkingTimeSlots = WorkingTimeSlots(List(Classroom(5)))
  val interval_2: WorkingTimeSlots = WorkingTimeSlots(List(Classroom(5), Classroom(6)))

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