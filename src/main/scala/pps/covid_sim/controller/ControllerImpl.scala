package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.samples
import pps.covid_sim.model.samples.Provinces
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq

class ControllerImpl extends Controller {

  override def startSimulation(from: Calendar, until: Calendar, runs: Int): Unit = ???

  override def tick(time: Calendar): Unit = ???

  override def simulationEnded(simulation: Simulation): Unit = {//TODO
    //chiamare un metodo sulla gui che gli dica di mostrare i grafici e glieli passo come argomenti: i grafici saranno di tipo page che contine titolo page(nome del grafico) e panel del grafico
  }

  override def startLockdown(time: Calendar, infections: Int): Unit = ???

  override def endLockdown(time: Calendar, infections: Int): Unit = ???

  override def regions: Seq[Region] = Seq(Region.EMILIA_ROMAGNA,Region.FRIULI_VENEZIA_GIULIA)

  override def provinces: Seq[Province] = Seq(Provinces.BOLOGNA, Provinces.MILANO, Provinces.ROMA, Provinces.TORINO)

  override def people: ParSeq[Person] = ParSeq(
    Student(ScalaCalendar(1997,1,1,1),Locality.City(1,"Bologna",1,Provinces.BOLOGNA)),
    Student(ScalaCalendar(1997,1,1,1),Locality.City(2,"Milano",1,Provinces.MILANO)),
    Student(ScalaCalendar(1997,1,1,1),Locality.City(3,"Roma",1,Provinces.ROMA)),
    Student(ScalaCalendar(1997,1,1,1),Locality.City(4,"Torino",1,Provinces.TORINO)),
  )

  def setSimulationParameters(safeZone: Double,minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int, multipleInfectionProbability: Double,
                              asymptomaticProbability: Int ,asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,minMaskProbability: Double ,maxMaskProbability : Int,
                              notRespectingIsolationMaxProbability: Double ,lockDownStart:Double,lockDownEnd: Double)










}
