package pps.covid_sim.controller
import java.util.Calendar

import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.{Person, PersonTmp}
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.util.time.Time
import pps.covid_sim.util.time.Time.ScalaCalendar

class ControllerImpl extends Controller {

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
