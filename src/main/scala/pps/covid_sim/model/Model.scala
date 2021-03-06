package pps.covid_sim.model

import java.util.Calendar

import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}

trait Model {

  val covidInfectionParameters: CovidInfectionParameters = CovidInfectionParameters()

  /**
   * Set the main simulation parameters.
   */
  def setSimulationParameters(safeZone: Double,
                              minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                              multipleInfectionProbability: Double,
                              asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,
                              minMaskProbability: Double, maxMaskProbability : Int,
                              averageSocialDistance: Double,
                              notRespectingIsolationMaxProbability: Double,
                              lockDownStart:Double, lockDownEnd: Double,
                              closedPlaceSet: Set[Class[_ <: Place]]): Unit

  /**
   * Init world, by creating places, people and cities according to the specified area.
   * @param area  the desired area on which simulate
   */
  def initWorld(area: Area): Unit

  /**
   * Init simulation, specifying the start and end and the number of runs.
   * @param area    the area on which simulate
   * @param from    the start of simulation
   * @param until   the end of simulation
   * @param runs    the number of runs
   */
  def initSimulation(area: Area, from: Calendar, until: Calendar, runs: Int)

  /**
   * Notify that a new hour has been started.
   * @param time  the current simulation time
   */
  def tick(time: Calendar): Unit

  /**
   * Notify the end of a run, collecting last data and clearing data structures.
   */
  def notifyRunEnded(): Unit

  /**
   * Notify the end of the entire simulation, destroying all world.
   */
  def notifySimulationEnded(): Unit

  /**
   * Get the SimulationManager instance.
   * @return  the SimulationManager instance
   */
  def simulationsManager: SimulationsManager[Simulation]

}
