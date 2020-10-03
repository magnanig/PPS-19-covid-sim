package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Place

import scala.collection.SortedMap

case class SimulationsManager[+S <: Simulation](simulations: Seq[S],
                                               area: Area,
                                                from: Calendar,
                                                until: Calendar) extends Iterable[S] {
  private var current: Int = 0

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

  def takeScreenshot(time: Calendar): Unit = {
    simulations(current).takeScreenshot(time)
  }

  override def iterator: Iterator[S] = simulations.iterator

}

object SimulationsManager {
  implicit val classOrdering: Ordering[Class[_ <: Place]] = (x: Class[_], y: Class[_]) =>
    x.getSimpleName.compareTo(y.getSimpleName)
}
