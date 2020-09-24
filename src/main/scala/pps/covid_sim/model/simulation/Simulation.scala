package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.Statistic

import scala.collection.SortedMap

case class Simulation(people: Seq[Person]) {

  private var _infected: SortedMap[Calendar, Int] = SortedMap()

  def infected: SortedMap[Calendar, Int] = _infected

  def updateInfectedCount(time: Calendar, count: Int): Unit = {
    _infected = _infected + (time -> count)
  }


  //TODO
  def close(): Unit = {
    println()
    println("FINE!!")
  }

}
