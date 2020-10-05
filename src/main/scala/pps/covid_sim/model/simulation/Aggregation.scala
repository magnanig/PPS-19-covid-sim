package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.places.Locality._
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place

import scala.collection.SortedMap

object Aggregation {

  abstract class AggregateSimulation[A <: Area](simulations: Traversable[Simulation]) extends Simulation {

    def apply(area: A): Option[Simulation] = simulations.find(_.area == area)

    override def infectionPlaces: Map[Class[_ <: Location], Int] = simulations.flatMap(_.infectionPlaces).toMap
      .groupBy(_._1)
      .mapValues(_.values.sum)

    override def infected: SortedMap[Calendar, Int] = aggregateValues(_.infected)

    override def recovered: SortedMap[Calendar, Int] = aggregateValues(_.recovered)

    override def deaths: SortedMap[Calendar, Int] = aggregateValues(_.deaths)

    override def takeScreenshot(time: Calendar): Unit = simulations.foreach(_.takeScreenshot(time))

    override def close(): Unit = simulations.foreach(_.close())

    private def aggregateValues(parameterSelection: Simulation => SortedMap[Calendar, Int]): SortedMap[Calendar, Int] = {
      SortedMap[Calendar, Int]() ++ simulations
        .flatMap(parameterSelection)
        .groupBy(_._1)
        .mapValues(_.map(_._2).sum)
    }
  }

  case class ProvinceSimulation(override val area: Province)
    extends AggregateSimulation[City](createCitySimulations(area))

  case class RegionSimulation(override val area: Region)
    extends AggregateSimulation[Province](createProvinceSimulations(area))

  case class NationSimulation() extends AggregateSimulation[Region](createRegionSimulations()) {
    override val area: Area = Italy()
  }

  private def createCitySimulations(province: Province): Set[CitySimulation] = CitiesContainer.getCities(province)
    .map(CitySimulation)

  private def createProvinceSimulations(region: Region): Set[ProvinceSimulation] = CitiesContainer.getProvince(region)
    .map(ProvinceSimulation)

  private def createRegionSimulations(): Set[RegionSimulation] = CitiesContainer.getRegions.map(RegionSimulation)

}
