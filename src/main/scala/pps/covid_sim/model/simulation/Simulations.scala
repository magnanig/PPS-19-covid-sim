package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.places.Place

import scala.collection.SortedMap

case class Simulations[S <: Simulation](from: Calendar, until: Calendar, runs: Int) extends Iterable[S] {

  private var simulations: Seq[S] = Seq()
  private var remaining: Int = runs

  def addSimulation(simulation: S): Unit = {
    remaining = remaining - 1
    simulations = simulation +: simulations
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

  def areCompleted: Boolean = remaining <= 0

  override def iterator: Iterator[S] = simulations.iterator

}

object Simulations {
  implicit val classOrdering: Ordering[Class[_ <: Place]] = (x: Class[_], y: Class[_]) =>
    x.getSimpleName.compareTo(y.getSimpleName)
}
