package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.controller.actors.coordinators.ActorsCoordination
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}
import pps.covid_sim.model.{CovidInfectionParameters, Model}
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.view.View

import scala.collection.parallel.ParSeq

class ControllerImpl(model: Model, view: View) extends Controller {

  override def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    model.initWorld(area)
    model.initSimulation(area, from, until, runs)
    view.notifyStart()
    startActors(model.simulationsManager)
  }

  override def tick(time: Calendar): Unit = {
    model.tick(time)
  }

  override def notifyRunEnded(): Unit = {
    view.notifyEndRun(model.simulationsManager.currentSimulation)
    model.simulationsManager.runCompleted()
    model.reset()
    if(!model.simulationsManager.hasEnded) {
      startActors(model.simulationsManager)
    } else {
      view.notifyEnd(model.simulationsManager)
      view.setVisibleConfirmButton() //riattiva il button
    }
  }

  override def startLockdown(time: Calendar, infections: Int): Unit = view.startLockdown(time,infections) //lineChart.drawLockDownStart(time, infections)

  override def endLockdown(time: Calendar, infections: Int): Unit = view.endLockdown(time,infections) //lineChart.drawLockDownEnd(time, infections)

  override def people: ParSeq[Person] = model.people

  override def setSimulationParameters(safeZone: Double,
                                       minRecoverTime: Int, maxRecoverTime: Int,
                                       minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                                       multipleInfectionProbability: Double,
                                       asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                                       contagionProbability: Double,
                                       minMaskProbability: Double, maxMaskProbability: Int,
                                       notRespectingIsolationMaxProbability: Double,
                                       lockDownStart: Double, lockDownEnd: Double, closedPlaceSet: Set[Class[_ <:Place]]): Unit = {
    model.setSimulationParameters(safeZone, minRecoverTime, maxRecoverTime,
      minInfectionDetectionTime, maxInfectionDetectionTime,
      multipleInfectionProbability, asymptomaticProbability,
      asymptomaticDetectionCondProbability, contagionProbability,
      minMaskProbability, maxMaskProbability,
      notRespectingIsolationMaxProbability, lockDownStart, lockDownEnd, closedPlaceSet)
  }

  private def startActors(simulationsManager: SimulationsManager[Simulation]): Unit = {
    ActorsCoordination.create(simulationsManager.area, this, simulationsManager.period)
  }

  override def covidInfectionParameters: CovidInfectionParameters = model.covidInfectionParameters

  override def simulationInterval: DatesInterval = model.simulationsManager.period
}
