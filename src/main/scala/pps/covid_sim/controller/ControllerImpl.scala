package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.Model
import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.parameters.CovidInfectionParameters

import scala.collection.parallel.ParSeq

class ControllerImpl(model: Model) extends Controller {

  override def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    model.initWorld(area)
    model.initSimulation(area, from, until, runs)
    startActors(area, from, until) // TODO x Sute, vedi in fondo
  }

  override def tick(time: Calendar): Unit = {
    model.tick(time)
  }

  override def notifyRunEnded(): Unit = {
    //TODO aggiungere, sulla view, la seguente simulazione/run (linea 2D):
    // model.simulationsManager.currentSimulation.{infected/recovered/deaths}

    // chiamare un metodo sulla gui che gli dica di mostrare i grafici e glieli passo come argomenti:
    // i grafici saranno di tipo page che contine titolo page(nome del grafico) e panel del grafico

    // N.B. aggiornare il grafico PRIMA delle seguenti righe

    model.simulationsManager.runCompleted()
    if(!model.simulationsManager.hasEnded) {
      model.reset()
      //startActors(...)
    } else {
      // TODO: notificare alla view che la simulazione è completamente terminata.
      //  Non c'è da fare molto, se non riattivare il pulsante "Start" per poter
      //  avviare una nuova simulazione
    }
  }

  // TODO: disegnare, se possibile, un simbolo sul grafico per marcare il punto di inizio e fine del lockdown,
  //  altrimenti rimuovere i seguenti due metodi
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
    CovidInfectionParameters.safeZone = safeZone
    CovidInfectionParameters.minRecoverTime = minRecoverTime
    CovidInfectionParameters.maxRecoverTime = maxRecoverTime
    CovidInfectionParameters.minInfectionDetectionTime = minInfectionDetectionTime
    CovidInfectionParameters.maxInfectionDetectionTime = maxInfectionDetectionTime
    CovidInfectionParameters.multipleInfectionProbability = multipleInfectionProbability
    //CovidInfectionParameters.asymptomaticProbability = asymptomaticProbability
    CovidInfectionParameters.asymptomaticDetectionCondProbability = asymptomaticDetectionCondProbability
    CovidInfectionParameters.contagionProbability = contagionProbability
    CovidInfectionParameters.minMaskProbability = minMaskProbability
    CovidInfectionParameters.maxMaskProbability = maxMaskProbability
    CovidInfectionParameters.notRespectingIsolationMaxProbability = notRespectingIsolationMaxProbability
    CovidInfectionParameters.lockDownStart = lockDownStart
    CovidInfectionParameters.lockDownEnd = lockDownEnd
  }

  private def startActors(area: Area, from: Calendar, until: Calendar): Unit = {
    // TODO x Sute:
    //  (1) se l'area è una città o provincia, creare solo il coordinatore di provincia;
    //  (2) se l'area è una regione, creare il coordinatore di regione e delle rispettive province;
    //  (3) se l'area è tutta italia, creare tutti i 3 coordinatori: top-level, di regione e di provincia
    //  Note: nel caso (1), il coordinatore di provincia opererà come se fosse di top level, ovvero sarà lui
    //        a prendere l'iniziativa di inviare il tick agli attori e non dovrà inviare nessun tick al livello
    //        superiore (che nemmeno esisterà).
    //        Nel caso (2) la situazione è analoga, ma stavolta è il coordinatore di regione ad operare a top-level,
    //        inviando il tick ai coordinatori sottostanti (di provincia).
    //  Nota2: non è più necessario avere l'oggetto Simulation dentro i coordinatori => li ho gestiti dal model;
    //         l'unica cosa fondamentale è richiamare il tick sul controller e richiamare notifyRunEnded quando
    //         il coordinatore ha terminato la simulazione corrente
    area match {
      case city: City => // (1)
      case province: Province => // (1)
      case region: Region => // (2)
      case _ => // (3)
    }

    // creazione new linechart grafico e init()
    //ActorsCoordination.initPeopleFriends(people)
    //ActorsCoordination.create(this, from -> until)
  }
}
