package pps.covid_sim.controller

import java.util.Calendar

import javax.swing.JPanel
import pps.covid_sim.controller.actors.ActorsCoordination
import pps.covid_sim.model.creation.CitiesObject
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}
import pps.covid_sim.model.{CovidInfectionParameters, Model}
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.view.{HeatMap, LineChart, View}

import scala.collection.SortedMap
import scala.collection.parallel.ParSeq
import scala.swing.Component
import scala.swing.TabbedPane.Page

class ControllerImpl(model: Model, view: View) extends Controller {

  val lineChart: LineChart = LineChart("Evoluzioni contagi", "asse x", "asse y", "legend") // spostare

  override def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    model.initWorld(area)
    model.initSimulation(area, from, until, runs)
    startActors(model.simulationsManager)

    val forli: City = City(1, "Forlì", 118000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.22268559, 12.04068608)
    val cesena: City = City(1, "Cesena", 98000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.13654899, 12.24217492)
    val fake: City = City(1, "Cesena", 98000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.80436680, 11.34172080)
    val rimini: City = City(1, "Rimini", 300000, Province(3, "Rimini", "RN", Locality.Region.EMILIA_ROMAGNA), 44.06090086, 12.56562951)
    val bologna: City = City(1, "Bologna", 1118000, Province(4, "Bologna", "BO", Locality.Region.EMILIA_ROMAGNA), 44.49436680, 11.34172080)

    val infectionsInADay = Map(forli -> 100 , cesena -> 4890, rimini -> 15001, bologna -> 800000, fake -> 100)

    //new HeatMap().drawMap(infectionsInADay)
    //new HeatMap().drawMap(infectionsInADay)
    val c = new HeatMap().drawMap(infectionsInADay)

    val scalaComp = new Component {
      override lazy val peer: JPanel = c
    }

    val page = new Page("titolo", scalaComp)
    view.insertTab(page)

    val data: SortedMap[Calendar, Int] = SortedMap(ScalaCalendar(2020, 9, 1, 15) -> 10, ScalaCalendar(2020, 9, 2, 15) -> 15, ScalaCalendar(2020, 9, 3, 15) -> 8)
    val chart = LineChart("Infections trend", "Days", "# infections", "Infections trend")
    chart.init(ScalaCalendar(2020, 9, 1, 15))
    chart.drawLockDownStart(ScalaCalendar(2020, 9, 1, 15), 10)
    chart.drawLockDownEnd(ScalaCalendar(2020, 9, 2, 15), 15)
    chart.drawLockDownStart(ScalaCalendar(2020, 9, 3, 15), 8)


    val scalaComp2 = new Component {
      override lazy val peer: JPanel = chart.drawChart(data)
    }

    val page2 = new Page("titolo2", scalaComp2)
    view.insertTab(page2)

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
      // Line Chart

      //model.simulationsManager.average(model.simulationsManager.map(_.recovered).toList)
      //model.simulationsManager.average(model.simulationsManager.map(_.deaths).toList))

      //
      //model.simulationsManager.average(model.simulationsManager.map(_.infectionPlaces).toList)

      view.setVisibleConfirmButton()//riattiva il button


      val page3 = new Page("Infections", convertJavaToScalaComponent(lineChart.drawChart(model.simulationsManager.average(model.simulationsManager.map(_.infected).toList))))
      view.insertTab(page3)



    }
  }

  private def convertJavaToScalaComponent(panel: JPanel): Component = {
    new Component {
      override lazy val peer: JPanel = panel
    }
  }

  // TODO: disegnare, se possibile, un simbolo sul grafico per marcare il punto di inizio e fine del lockdown,
  //  altrimenti rimuovere i seguenti due metodi       [X MELUZ credo (by Sute)]
  override def startLockdown(time: Calendar, infections: Int): Unit = lineChart.drawLockDownStart(time, infections)
  override def endLockdown(time: Calendar, infections: Int): Unit = lineChart.drawLockDownEnd(time, infections)

  override def regions: Set[Region] = CitiesObject.getRegions

  override def provinces: Set[Province] = regions.flatMap(CitiesObject.getProvince)

  override def cities: Set[City] = CitiesObject.getCities

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
