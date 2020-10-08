package pps.covid_sim.util.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.scheduling.Planning.CustomPlan
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class AgendaTest {

  val location1: Location = null

  val location2: Location = null

  val agenda: Agenda = Agenda(null)
  agenda.addPlan(CustomPlan(enabledInLockdown = false)
    .add(location1, Day.MONDAY -> Day.TUESDAY,  8 -> 13)
    .add(location2, Day.WEDNESDAY,8 -> 12)
    .add(location1, Day.THURSDAY -> Day.FRIDAY, 8 -> 13)
    .dayPlan(Day.SATURDAY)
      .add(location1, 8 -> 10)
      .add(location2, 10 -> 12)
      .add(location1, 12 -> 13)
      .commit()
  )

  @Test
  def testEmptyAgenda(): Unit = {
    val emptyAgenda = Agenda(null)
    val from = ScalaCalendar(2020, 8, 1)
    assertEquals(from -> ScalaCalendar(2020, 8, 1, 12), emptyAgenda.firstNextFreeTime(from)) // implicitly limits by 12h
  }

  @Test
  def testOnlyPlan(): Unit = {
    implicit val limit: Int = 100
    var time = ScalaCalendar(2020, 8, 18, 7) // Mar 18 Agosto h 7.00: ok, I'm free from 7 to 8, then I'm busy
    assertEquals(ScalaCalendar(2020, 8, 18, 7) -> ScalaCalendar(2020, 8, 18, 8), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 8) // too late... now I'm busy but later I'm free until tomorrow morning
    assertEquals(ScalaCalendar(2020, 8, 18, 13) -> ScalaCalendar(2020, 8, 19, 8), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 13) // I've just ended my plan and I'm free until tomorrow morning
    assertEquals(ScalaCalendar(2020, 8, 18, 13) -> ScalaCalendar(2020, 8, 19, 8), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 16) // I'm free until tomorrow morning
    assertEquals(ScalaCalendar(2020, 8, 18, 16) -> ScalaCalendar(2020, 8, 19, 8), agenda.firstNextFreeTime(time))

    assert(agenda.isFreeAt(ScalaCalendar(2020, 8, 18, 7)))
    assert(!agenda.isFreeAt(ScalaCalendar(2020, 8, 18, 8)))
    assert(agenda.isFreeAt(ScalaCalendar(2020, 8, 18, 13)))
  }

  @Test
  def testPlanAndFixedAppointments(): Unit = {
    addFixedAppointments()
    var time = ScalaCalendar(2020, 8, 18, 12)
    assertEquals(ScalaCalendar(2020, 8, 18, 13) -> ScalaCalendar(2020, 8, 18, 16), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 15)
    assertEquals(ScalaCalendar(2020, 8, 18, 15) -> ScalaCalendar(2020, 8, 18, 16), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 16)
    assertEquals(ScalaCalendar(2020, 8, 18, 20) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 20)
    assertEquals(ScalaCalendar(2020, 8, 18, 20) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 21)
    assertEquals(ScalaCalendar(2020, 8, 18, 21) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 22)
    assertEquals(ScalaCalendar(2020, 8, 18, 23) -> ScalaCalendar(2020, 8, 19, 8), agenda.firstNextFreeTime(time))
  }

  @Test
  def testFullAgenda(): Unit = {
    addFixedAppointments()
    agenda.joinAppointment(Appointment(ScalaCalendar(2020, 8, 18, 14), null))

    var time = ScalaCalendar(2020, 8, 18, 12)
    assertEquals(ScalaCalendar(2020, 8, 18, 13) -> ScalaCalendar(2020, 8, 18, 14), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 15)
    assertEquals(ScalaCalendar(2020, 8, 18, 15) -> ScalaCalendar(2020, 8, 18, 16), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 16)
    assertEquals(ScalaCalendar(2020, 8, 18, 20) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 20)
    assertEquals(ScalaCalendar(2020, 8, 18, 20) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 21)
    assertEquals(ScalaCalendar(2020, 8, 18, 21) -> ScalaCalendar(2020, 8, 18, 22), agenda.firstNextFreeTime(time))
    time = ScalaCalendar(2020, 8, 18, 22)
    assertEquals(ScalaCalendar(2020, 8, 18, 23) -> ScalaCalendar(2020, 8, 19, 8), agenda.firstNextFreeTime(time))
  }

  private def addFixedAppointments(): Unit = {
    agenda.fixAppointment(Appointment(ScalaCalendar(2020, 8, 18, 16) -> ScalaCalendar(2020, 8, 18, 20), null), null)
    agenda.fixAppointment(Appointment(ScalaCalendar(2020, 8, 18, 22), null), null)
  }

}
