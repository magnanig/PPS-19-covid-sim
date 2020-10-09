package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

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

  /**
   * Notify the start of lockdown.
   * @param time          the actual time
   * @param infections    the number of infections when lockdown started
   */
  def startLockdown(time: Calendar, infections: Int): Unit

  /**
   * Notify the end of lockdown.
   * @param time          the actual time
   * @param infections    the number of infections when lockdown ended
   */
  def endLockdown(time: Calendar, infections: Int): Unit

  /**
   * Get covid infection parameters.
   * @return  the covid infection parameters
   */
  def covidInfectionParameters: CovidInfectionParameters

  /**
   * Get the interval of which simulation refers to.
   * @return  the simulation interval
   */
  def simulationInterval: DatesInterval

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

}
