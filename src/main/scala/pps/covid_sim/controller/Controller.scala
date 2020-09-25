package pps.covid_sim.controller

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.simulation.Simulation

trait Controller {

  def startSimulation(from: Calendar, until: Calendar, runs: Int)

  def tick(time: Calendar): Unit

  def simulationEnded(simulation: Simulation): Unit

  def startLockdown(time: Calendar, infections: Int): Unit

  def endLockdown(time: Calendar, infections: Int): Unit

  def regions: Seq[Region]

  def provinces: Seq[Province]

  def people: Seq[Person]

}
