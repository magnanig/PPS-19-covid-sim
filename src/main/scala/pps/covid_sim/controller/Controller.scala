package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq

trait Controller {

  def startSimulation(from: Calendar, until: Calendar, runs: Int)

  def tick(time: Calendar): Unit

  def simulationEnded(simulation: Simulation): Unit

  def startLockdown(time: Calendar, infections: Int): Unit

  def endLockdown(time: Calendar, infections: Int): Unit

  def setSimulationParameters(safeZone: Double,minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int, multipleInfectionProbability: Double,
                              asymptomaticProbability: Double ,asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,minMaskProbability: Double ,maxMaskProbability : Int,
                              notRespectingIsolationMaxProbability: Double ,lockDownStart:Double,lockDownEnd: Double): Unit
  def setDateSimulation(dataInizio: ScalaCalendar,dataFine: ScalaCalendar)

  def regions: Seq[Region]

  def provinces: Seq[Province]

  def people: ParSeq[Person]



}
