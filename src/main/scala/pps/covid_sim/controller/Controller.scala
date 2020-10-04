package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

import scala.collection.parallel.ParSeq

trait Controller {

  def startSimulation(area: Area, from: Calendar, until: Calendar, runs: Int)

  def tick(time: Calendar): Unit

  def notifyRunEnded(): Unit

  def startLockdown(time: Calendar, infections: Int): Unit

  def endLockdown(time: Calendar, infections: Int): Unit

  def setSimulationParameters(safeZone: Double,
                              minRecoverTime: Int, maxRecoverTime: Int,
                              minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                              multipleInfectionProbability: Double,
                              asymptomaticProbability: Double, asymptomaticDetectionCondProbability: Double,
                              contagionProbability: Double,
                              minMaskProbability: Double, maxMaskProbability : Int,
                              notRespectingIsolationMaxProbability: Double,
                              lockDownStart:Double, lockDownEnd: Double,
                              closedPlaceSet: Set[Class[_ <:Place]]): Unit

  def covidInfectionParameters: CovidInfectionParameters

  def regions: Set[Region]

  def provinces: Set[Province]

  def cities: Set[City]

  def people: ParSeq[Person]

  def simulationInterval: DatesInterval

}
