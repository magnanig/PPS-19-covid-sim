package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.Statistic
import pps.covid_sim.model.container.PeopleContainer
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location

import scala.collection.SortedMap

case class CitySimulation(override val area: City) extends Simulation {
  private var _infected: SortedMap[Calendar, Int] = SortedMap()
  private var _recovered: SortedMap[Calendar, Int] = SortedMap()
  private var _deaths: SortedMap[Calendar, Int] = SortedMap()
  private var _infectionPlaces: Map[Class[_ <: Location], Int] = Map()

  override def infectionPlaces: Map[Class[_ <: Location], Int] = _infectionPlaces

  override def infected: SortedMap[Calendar, Int] = _infected

  override def recovered: SortedMap[Calendar, Int] = _recovered

  override def deaths: SortedMap[Calendar, Int] = _deaths

  private[model] override def takeScreenshot(time: Calendar): Unit = {
    _infected = updateParameter(infected, time, Statistic(PeopleContainer.people).numCurrentPositive(area))
    _recovered = updateParameter(recovered, time, Statistic(PeopleContainer.people).numRecovered(area))
    //_deaths = updateParameter(deaths, time, Statistic(PeopleContainer.getPeople.par).numDeaths(area))
  }

  private[model] override def close(): Unit = {
    _infectionPlaces = Statistic(PeopleContainer.people.par).getInfectionsPerPlace
  }

  private def updateParameter(parameter: SortedMap[Calendar, Int],
                              time: Calendar,
                              value: Int): SortedMap[Calendar, Int] = parameter + (time -> value)
}
