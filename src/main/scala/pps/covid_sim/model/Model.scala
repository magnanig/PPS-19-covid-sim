package pps.covid_sim.model

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}

import scala.collection.parallel.ParSeq

trait Model {

  def initWorld(area: Area): Unit

  def initSimulation(area: Area, from: Calendar, until: Calendar, runs: Int)

  def people: ParSeq[Person]

  def tick(time: Calendar): Unit

  def reset(): Unit

  def simulationsManager: SimulationsManager[Simulation]

}
