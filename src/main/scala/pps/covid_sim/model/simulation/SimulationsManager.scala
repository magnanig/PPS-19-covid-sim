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

  /**
   * Get the n-th simulation (i.e. run).
   * @param index the index of desired simulation
   * @return  the index-th simulation
   */
  def apply(index: Int): S = simulations(index)

  /**
   * Notify the current run has been completed.
   */
  def runCompleted(): Unit = {
    simulations(current).close()
    current = current + 1
  }

  /**
   * Compute the average of the specified list of maps.
   * @param values  the list of map whose values average is needed
   * @tparam A      the type of map values
   * @return        a sorted map with the average of the input list of map
   */
  def average[A](values: => List[Map[A, Int]])(implicit ordering: Ordering[A]): SortedMap[A, Int] = {
    average(values.map(SortedMap[A, Int]() ++ _))
  }

  /**
   * Compute the average of the specified list of maps.
   * @param values  the list of map whose values average is needed
   * @tparam A      the type of map values
   * @return        a sorted map with the average of the input list of map
   */
  def average[A](values: List[SortedMap[A, Int]])(implicit ordering: Ordering[A]): SortedMap[A, Int] = {
    SortedMap[A, Int]() ++
      values
        .flatten
        .groupBy(_._1)
        .mapValues(_.map(_._2).sum / values.size)
  }

  /**
   * Check whether all simulations have been ended.
   * @return  true if all simulations have been ended, false otherwise
   */
  def hasEnded: Boolean = current >= simulations.size

  /**
   * Get the current simulation (i.e. run).
   * @return  the current simulation
   */
  def currentSimulation: S = simulations(current)

  /**
   * Take a screenshot for the specified time, saving the number of
   * infection, recovered people and deaths.
   * @param time  current time
   */
  def takeScreenshot(time: Calendar, people: ParSeq[Person]): Unit = {
    simulations(current).takeScreenshot(time)
    if(current == 0) {
      covidStages += (time -> Statistic(people).covidStages())
      _citiesInfection += (time -> Statistic(people).cityPositives(CitiesContainer.getCities(area)))
    }
  }

  /**
   * Get the number of infected for each city, obtained in the first simulation (i.e. control run)
   * @return  the number of infected for each city
   */
  def citiesInfection: SortedMap[Calendar, SortedMap[City, Int]] = _citiesInfection

  /**
   * Computes the weekly covid stage, i.e. the number of people who contracted covid-19 at each stage.
   * @return  a sorted map with the weekly covid stage
   */
  def weeklyCovidStages: SortedMap[Calendar, Map[Int, Int]] = SortedMap[Calendar, Map[Int, Int]]() ++
    covidStages.toList.grouped(7)
    .map(group => (group.last._1, group
      .flatMap(_._2)
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
    )).toMap

  /**
   * Computes the evolution day by day of each covid stage.
   * @return  a map that for each covid stage associates its evolution in time
   *          (i.e. the number of people who contracted the virus at that stage)
   */
  def dayCovidStages: Map[Int, SortedMap[Calendar, Int]] = covidStages.values.flatten.toMap.keySet
      .map(stage => stage -> (SortedMap[Calendar, Int]() ++
        covidStages.map(e => e._1 -> e._2.getOrElse(stage, 0)))).toMap

  override def iterator: Iterator[S] = simulations.iterator

}

object SimulationsManager {
  implicit val classOrdering: Ordering[Class[_ <: Location]] = (x: Class[_], y: Class[_]) =>
    x.getSimpleName.compareTo(y.getSimpleName)
}
