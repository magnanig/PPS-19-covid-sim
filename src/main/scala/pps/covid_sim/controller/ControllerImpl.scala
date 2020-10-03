package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.controller.actors.ActorsCoordination
import pps.covid_sim.model.{CovidInfectionParameters, Model}
import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}
import pps.covid_sim.parameters.CovidInfectionParameters
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.view.{LineChart, View}

import scala.collection.parallel.ParSeq

class ControllerImpl(model: Model, view: View) extends Controller {

  override def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    model.initWorld(area)
    model.initSimulation(area, from, until, runs)
    startActors(model.simulationsManager)
  }

  override def tick(time: Calendar): Unit = {
    model.tick(time)
  }

  override def notifyRunEnded(): Unit = {
    //TODO aggiungere, sulla view, la seguente simulazione/run (linea 2D):
    // model.simulationsManager.currentSimulation.{infected/recovered/deaths}

    //view.insertTab(/*grafico*/)

    // chiamare un metodo sulla gui che gli dica di mostrare i grafici e glieli passo come argomenti:
    // i grafici saranno di tipo page che contine titolo page(nome del grafico) e panel del grafico

    // N.B. aggiornare il grafico PRIMA delle seguenti righe



    model.simulationsManager.runCompleted()
    if(!model.simulationsManager.hasEnded) {
      model.reset()

      startActors(model.simulationsManager)
    } else {
      // TODO: notificare alla view che la simulazione è completamente terminata.
      //  Non c'è da fare molto, se non riattivare il pulsante "Start" per poter
      //  avviare una nuova simulazione
      view.setVisibleConfirmButton()//riattiva il button




    }
  }

  // TODO: disegnare, se possibile, un simbolo sul grafico per marcare il punto di inizio e fine del lockdown,
  //  altrimenti rimuovere i seguenti due metodi       [X MELUZ credo (by Sute)]
  override def startLockdown(time: Calendar, infections: Int): Unit = ???
  override def endLockdown(time: Calendar, infections: Int): Unit = ???

  override def regions: Set[Region] = CitiesContainer.getRegions

  override def provinces: Set[Province] = regions.flatMap(CitiesContainer.getProvince)

  override def cities: Set[City] = CitiesContainer.getCities

  override def people: ParSeq[Person] = model.people

  override def setSimulationParameters(safeZone: Double,
                                       minRecoverTime: Int, maxRecoverTime: Int,
                                       minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                                       multipleInfectionProbability: Double,
                                       asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                                       contagionProbability: Double,
                                       minMaskProbability: Double, maxMaskProbability: Int,
                                       notRespectingIsolationMaxProbability: Double,
                                       lockDownStart: Double, lockDownEnd: Double): Unit = {
    model.setSimulationParameters(safeZone, minRecoverTime, maxRecoverTime,
      minInfectionDetectionTime, maxInfectionDetectionTime,
      multipleInfectionProbability, asymptomaticProbability,
      asymptomaticDetectionCondProbability, contagionProbability,
      minMaskProbability, maxMaskProbability,
      notRespectingIsolationMaxProbability, lockDownStart, lockDownEnd)
  }

  private def startActors(simulationsManager: SimulationsManager[Simulation]): Unit = {
    ActorsCoordination.create(simulationsManager.area, this,
      new DatesInterval(simulationsManager.from,simulationsManager.until))
  }

  override def covidInfectionParameters: CovidInfectionParameters = model.covidInfectionParameters
}
