package pps.covid_sim.model
import java.util.Calendar

import pps.covid_sim.model.container.{PeopleContainer, PlacesContainer}
import pps.covid_sim.model.creation.{RegionPeopleCreation, RegionPlacesCreation, WorldCreation}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.simulation.Aggregation.{NationSimulation, ProvinceSimulation, RegionSimulation}
import pps.covid_sim.model.simulation.{CitySimulation, Simulation, SimulationsManager}

import scala.collection.parallel.ParSeq

class ModelImpl extends Model {

  private var places: ParSeq[Place] = _
  // private val trainLines = TODO
  // private val busLines = TODO

  private var _people: ParSeq[Person] = _
  private var _simulationsManager: SimulationsManager[Simulation] = _

  override val people: ParSeq[Person] = _people

  override def initWorld(area: Area): Unit = {
    area match {
      case Locality.Italy() => WorldCreation.generateAll()
      case _ => RegionPlacesCreation.create(area)
    }
    places = PlacesContainer.getPlaces.par
    _people = PeopleContainer.getPeople.par
    //trainLines = TrainLinesContainer.getLines.par
    //busLines = BusLinesContainer.getLines.par
  }

  override def initSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    _simulationsManager = area match {
      case city: City =>  SimulationsManager[CitySimulation]((1 to runs)
        .map(_ => CitySimulation(city)), from, until)
      case province: Province => SimulationsManager[ProvinceSimulation]((1 to runs)
        .map(_ => ProvinceSimulation(province)), from, until)
      case region: Region => SimulationsManager[RegionSimulation]((1 to runs)
        .map(_ => RegionSimulation(region)), from, until)
      case _ => SimulationsManager[NationSimulation]((1 to runs)
        .map(_ => NationSimulation()), from, until)
    }
  }

  override def tick(time: Calendar): Unit = {
    places.foreach(place => place.propagateVirus(time, place))
    simulationsManager.takeScreenshot(time)
    // trainLines.trains.foreach(transport => transport.propagateVirus(time, transport))
    // busLines.busses.foreach(transport => transport.propagateVirus(time, transport))
  }

  override def reset(): Unit = { places.foreach(_.clear()) }

  override def simulationsManager: SimulationsManager[Simulation] = _simulationsManager
}
