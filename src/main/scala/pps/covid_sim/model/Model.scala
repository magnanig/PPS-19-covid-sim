package pps.covid_sim.model

import java.util.Calendar

import pps.covid_sim.model.simulation.{Simulation, Simulations}
import pps.covid_sim.model.people.Person

trait Model {

  def startSimulation(from: Calendar, until: Calendar, runs: Int)

  def addSimulation(simulation: Simulation): Unit

  def tick(time: Calendar): Unit

  def reset(): Unit

  def simulations: Simulations[Simulation]

  def people: Seq[Person]



}
