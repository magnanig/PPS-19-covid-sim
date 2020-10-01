package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Province
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.Statistic

import scala.collection.SortedMap
import scala.collection.parallel.ParSeq

case class ProvinceSimulation(override val area: Province) extends Simulation {
  private var _infected: SortedMap[Calendar, Int] = SortedMap()
  private var _recovered: SortedMap[Calendar, Int] = SortedMap()
  private var _deaths: SortedMap[Calendar, Int] = SortedMap()
  private var _infectionPlaces: Map[Class[_ <: Place], Int] = Map()

  def updateInfectedCount(time: Calendar, count: Int): Unit = {
    _infected = updateParameter(infected, time, count)
  }

  def updateRecoveredCount(time: Calendar, count: Int): Unit = {
    _recovered = updateParameter(recovered, time, count)
  }

  def updateDeathsCount(time: Calendar, count: Int): Unit = {
    _deaths = updateParameter(deaths, time, count)
  }

  override def infectionPlaces: Map[Class[_ <: Place], Int] = _infectionPlaces

  override def infected: SortedMap[Calendar, Int] = _infected

  override def recovered: SortedMap[Calendar, Int] = _recovered

  override def deaths: SortedMap[Calendar, Int] = _deaths

  def close(people: ParSeq[Person]): Unit = {
    _infectionPlaces = Statistic(people).getInfectionsPerPlace
  }

  private def updateParameter(parameter: SortedMap[Calendar, Int],
                              time: Calendar,
                              value: Int): SortedMap[Calendar, Int] = parameter + (time -> value)
}
