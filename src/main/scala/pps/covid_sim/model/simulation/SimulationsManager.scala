package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City}
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.time.DatesInterval

import scala.collection.SortedMap
import scala.collection.parallel.ParSeq

case class SimulationsManager[+S <: Simulation](simulations: Seq[S],
                                               area: Area,
                                               period: DatesInterval) extends Iterable[S] {
  private var current: Int = 0
  private var _citiesInfection: SortedMap[Calendar, SortedMap[City, Int]] = SortedMap()
  private var covidStages: SortedMap[Calendar, Map[Int, Int]] = SortedMap()

  def apply(index: Int): S = simulations(index)

  def runCompleted(): Unit = {
    simulations(current).close()
    current = current + 1
  }

  def average[A](values: => List[Map[A, Int]])(implicit ordering: Ordering[A]): SortedMap[A, Int] = {
    average(values.map(SortedMap[A, Int]() ++ _))
  }

  def average[A](values: List[SortedMap[A, Int]])(implicit ordering: Ordering[A]): SortedMap[A, Int] = {
    SortedMap[A, Int]() ++
      values
        .flatten
        .groupBy(_._1)
        .mapValues(_.map(_._2).sum / values.size)
  }

  def hasEnded: Boolean = current >= simulations.size

  def currentSimulation: S = simulations(current)

  def takeScreenshot(time: Calendar, people: ParSeq[Person]): Unit = {
    simulations(current).takeScreenshot(time)
    if(current == 0) {
      covidStages += (time -> Statistic(people).covidStages())
      _citiesInfection += (time -> Statistic(people).cityPositives(CitiesContainer.getCities(area)))
    }
  }

  def citiesInfection: SortedMap[Calendar, SortedMap[City, Int]] = _citiesInfection

  def weeklyCovidStages: SortedMap[Calendar, Map[Int, Int]] = SortedMap[Calendar, Map[Int, Int]]() ++
    covidStages.toList.grouped(7)
    .map(group => (group.last._1, group
      .flatMap(_._2)
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
    )).toMap

  def dayCovidStages: Map[Int, SortedMap[Calendar, Int]] = covidStages.values.flatten.toMap.keySet
      .map(stage => stage -> (SortedMap[Calendar, Int]() ++
        covidStages.map(e => e._1 -> e._2.getOrElse(stage, 0)))).toMap

  override def iterator: Iterator[S] = simulations.iterator

}

object SimulationsManager {
  implicit val classOrdering: Ordering[Class[_ <: Location]] = (x: Class[_], y: Class[_]) =>
    x.getSimpleName.compareTo(y.getSimpleName)
}
