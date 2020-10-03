package pps.covid_sim.model

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}

import scala.collection.parallel.ParSeq

trait Model {

  val covidInfectionParameters: CovidInfectionParameters = CovidInfectionParameters()

  def setSimulationParameters(safeZone: Double,
                              minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                              multipleInfectionProbability: Double,
                              asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,
                              minMaskProbability: Double, maxMaskProbability : Int,
                              notRespectingIsolationMaxProbability: Double,
                              lockDownStart:Double, lockDownEnd: Double): Unit

  def initWorld(area: Area): Unit

  def initSimulation(area: Area, from: Calendar, until: Calendar, runs: Int)

  def people: ParSeq[Person]

  def tick(time: Calendar): Unit

  def reset(): Unit

  def simulationsManager: SimulationsManager[Simulation]

}
