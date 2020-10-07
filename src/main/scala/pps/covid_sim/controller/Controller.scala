package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

import scala.collection.parallel.ParSeq

trait Controller {

  /**
   * Start the simulation, specifying the start and end and the number of runs.
   * @param area    the area on which simulate
   * @param from    the start of simulation
   * @param until   the end of simulation
   * @param runs    the number of runs
   */
  def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int)

  /**
   * Notify that a new hour has been started.
   * @param time  the current simulation time
   */
  def tick(time: Calendar): Unit

  /**
   * Notify the end of current run.
   */
  def notifyRunEnded(): Unit

  def startLockdown(time: Calendar, infections: Int): Unit

  def endLockdown(time: Calendar, infections: Int): Unit

  /**
   * Set the main simulation parameters.
   */
  def setSimulationParameters(safeZone: Double,
                              minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                              multipleInfectionProbability: Double,
                              asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,
                              minMaskProbability: Double, maxMaskProbability: Int,
                              averageSocialDistance: Double,
                              notRespectingIsolationMaxProbability: Double,
                              lockDownStart:Double, lockDownEnd: Double,
                              closedPlaceSet: Set[Class[_ <: Place]]): Unit

  /**
   * Get covid infection parameters.
   * @return  the covid infection parameters
   */
  def covidInfectionParameters: CovidInfectionParameters

  /**
   * The list of people.
   * @return  the list of people
   */
  def people: ParSeq[Person]

  /**
   * Get the interval of which simulation refers to.
   * @return  the simulation interval
   */
  def simulationInterval: DatesInterval

}
