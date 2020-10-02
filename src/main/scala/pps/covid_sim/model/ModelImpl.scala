package pps.covid_sim.model
import java.util.Calendar

import pps.covid_sim.model.container.PlacesContainer
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.simulation.{Simulation, Simulations}

class ModelImpl extends Model {

  private val places = PlacesContainer.getPlaces.par
  // private val transports = TODO
  private var _simulations: Simulations[Simulation] = _

  override def startSimulation(from: Calendar, until: Calendar, runs: Int): Unit = {
    _simulations = Simulations(from, until, runs)
  }

  override def addSimulation(simulation: Simulation): Unit = { _simulations.addSimulation(simulation) }

  override def tick(time: Calendar): Unit = {
    places.foreach(place => place.propagateVirus(time, place))
    // transports.foreach(transport => transport.propagateVirus(time, transport))
  }

  override def reset(): Unit = { places.foreach(_.clear()) }

  override def simulations: Simulations[Simulation] = _simulations

  override def people: Seq[Person] = ???
}
