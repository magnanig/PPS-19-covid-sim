package pps.covid_sim.controller.Coordination

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.controller.{Controller, ControllerImpl}
import pps.covid_sim.controller.actors.ActorsCoordination
import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.simulation.Simulation
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
  val c = new ControllerImpl() {
    override def startSimulation(from: Calendar, until: Calendar, runs: Int): Unit = ???

    override def tick(time: Calendar): Unit = ???

    override def simulationEnded(simulation: Simulation): Unit = {}//TODO

    override def startLockdown(time: Calendar, infections: Int): Unit = ???

    override def endLockdown(time: Calendar, infections: Int): Unit = ???

    override def regions: Seq[Region] = Seq(Region.EMILIA_ROMAGNA,Region.FRIULI_VENEZIA_GIULIA)

    override def provinces: Seq[Province] = Seq(Province.BOLOGNA, Province.MILANO, Province.ROMA, Province.TORINO)

    override def people: Seq[Person] = Seq(
      Student(ScalaCalendar(1997,1,1,1),Locality.City(1,"Bologna",1,Province.BOLOGNA)),
      Student(ScalaCalendar(1997,1,1,1),Locality.City(2,"Milano",1,Province.MILANO)),
      Student(ScalaCalendar(1997,1,1,1),Locality.City(3,"Roma",1,Province.ROMA)),
      Student(ScalaCalendar(1997,1,1,1),Locality.City(4,"Torino",1,Province.TORINO)),
    )
  }
  val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
  val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

  val interval = new DatesInterval(start,end)
  ActorsCoordination.create(c,interval)
}
