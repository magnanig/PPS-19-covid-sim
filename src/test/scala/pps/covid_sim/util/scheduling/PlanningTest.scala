package pps.covid_sim.util.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.samples.Places
import pps.covid_sim.util.scheduling.Planning.CustomPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.{DaysInterval, HoursInterval}

class PlanningTest {

  val location1: Location = Places.BAR

  val location2: Location = Places.PARK

  // implicitly testing add(s) and commit
  val plan: CustomPlan[Location] = CustomPlan[Location]()
    .add(location1, Day.MONDAY, HoursInterval(8, 12))
    .add(location2, Day.WEDNESDAY, HoursInterval(14, 19))
    .dayPlan(Day.FRIDAY)
      .add(location1, HoursInterval(9, 13))
      .commit()
    .add(location2, DaysInterval.WEEKEND, HoursInterval(9, 20))
    .commit()

  @Test
  def testPlanDefinition(): Unit = {
    assert(plan.isDefinedOn(Day.MONDAY, HoursInterval(11, 15)))
    assert(plan.isDefinedOn(Day.MONDAY, 10)) // from 10:00 to 10:59 (= until 11:00)
    assert(!plan.isDefinedOn(Day.MONDAY, HoursInterval(7, 8)))
    assert(!plan.isDefinedOn(Day.MONDAY, HoursInterval(12, 15)))
    assert(!plan.isDefinedOn(Day.TUESDAY, HoursInterval(9, 20)))
    assert(plan.isDefinedOn(Day.MONDAY))
    assert(!plan.isDefinedOn(Day.TUESDAY))
    assert(plan.isDefinedOn(Day.FRIDAY))
    assert(plan.isDefinedOn(Day.SATURDAY))
    assert(plan.isDefinedOn(Day.SUNDAY))
  }

  @Test
  def testGetLocation(): Unit = {
    assertEquals(Some(location1), plan.location(Day.MONDAY, 10))
    assertEquals(None, plan.location(Day.MONDAY, 12))
    assertEquals(None, plan.location(Day.TUESDAY, 15))
    assertEquals(None, plan.location(Day.WEDNESDAY, 13))
    assertEquals(Some(location2), plan.location(Day.SATURDAY, 15))
  }

}
