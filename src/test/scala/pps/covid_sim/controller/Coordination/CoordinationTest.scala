package pps.covid_sim.controller.Coordination

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.controller.ControllerImpl
import pps.covid_sim.controller.actors.ActorsCoordination
import pps.covid_sim.util.time.{DatesInterval, HoursInterval, Time}
import pps.covid_sim.util.time.Time.ScalaCalendar

class CoordinationTest {


  @Test
  def testCoordinationWithActors(): Unit = {
    val c = new ControllerImpl
    val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
    val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

    val interval = new DatesInterval(start,end)
    ActorsCoordination.create(c,interval)
  }

}

object TestingCoordination extends App {
  val c = new ControllerImpl
  val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
  val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

  val interval = new DatesInterval(start,end)
  ActorsCoordination.create(c,interval)
}
