package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.places.Locality.{Area, Italy, Region}
import pps.covid_sim.model.places.Place

import scala.collection.SortedMap

object Aggregation {

  abstract class AggregateSimulation[A <: Area](simulations: Seq[Simulation]) extends Simulation {

    def apply(area: A): Option[Simulation] = simulations.find(_.area == area)

    override def infectionPlaces: Map[Class[_ <: Place], Int] = simulations.flatMap(_.infectionPlaces).toMap
      .groupBy(_._1)
      .mapValues(_.values.sum)

    override def infected: SortedMap[Calendar, Int] = aggregateValues(_.infected)

    override def recovered: SortedMap[Calendar, Int] = aggregateValues(_.recovered)

    override def deaths: SortedMap[Calendar, Int] = aggregateValues(_.deaths)

    private def aggregateValues(parameterSelection: Simulation => SortedMap[Calendar, Int]): SortedMap[Calendar, Int] = {
      SortedMap[Calendar, Int]() ++ simulations
        .flatMap(parameterSelection).toMap
        .groupBy(_._1)
        .mapValues(_.values.sum)
    }
  }

  case class RegionSimulation(override val area: Region, provinceSimulations: Seq[ProvinceSimulation])
    extends AggregateSimulation(provinceSimulations)

  case class NationSimulation(regionSimulations: Seq[RegionSimulation])
    extends AggregateSimulation(regionSimulations) {
    override val area: Area = Italy()
  }

}
