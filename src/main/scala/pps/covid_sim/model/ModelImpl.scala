package pps.covid_sim.model
import java.util.Calendar

import pps.covid_sim.model.container.{PeopleContainer, PlacesContainer, TransportLinesContainer}
import pps.covid_sim.model.creation.WorldCreation
import pps.covid_sim.model.creation.province.ProvinceCreation
import pps.covid_sim.model.creation.region.RegionCreation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality._
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.samples.Places
import pps.covid_sim.model.simulation.Aggregation.{NationSimulation, ProvinceSimulation, RegionSimulation}
import pps.covid_sim.model.simulation.{CitySimulation, Simulation, SimulationsManager}
import pps.covid_sim.model.transports.PublicTransports.{BusLine, TrainLine}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq
import scala.util.Random

class ModelImpl extends Model {

  private var places: ParSeq[Place] = _
   private var trainLines: ParSeq[TrainLine] = _
   private var busLines: ParSeq[BusLine] = _

  private var _people: ParSeq[Person] = _
  private var _simulationsManager: SimulationsManager[Simulation] = _

  override def people: ParSeq[Person] = _people

  override def initWorld(area: Area): Unit = {
    area match {
      case Italy() => WorldCreation.create()
      case region: Region => RegionCreation.create(region)
      case province: Province => ProvinceCreation.create(province)
    }
    places = PlacesContainer.getPlaces.par
    _people = Random.shuffle(PeopleContainer.getPeople/*.take(1000)*/).par
    initPeopleFriends(_people)
    println(s"Created ${_people.size} people")
    trainLines = TransportLinesContainer.getTrainLines.par
    busLines = TransportLinesContainer.getBusLines.par
  }

  override def initSimulation(area: Area, from: Calendar, until: Calendar, runs: Int): Unit = {
    import pps.covid_sim.util.time.TimeIntervalsImplicits._
    Random.shuffle(_people.seq).take(5).foreach(_.infects(Places.BAR, from, 0)(covidInfectionParameters))
    _simulationsManager = area match {
      case city: City =>  SimulationsManager[CitySimulation]((1 to runs)
        .map(_ => CitySimulation(city)),area, from -> until)
      case province: Province => SimulationsManager[ProvinceSimulation]((1 to runs)
        .map(_ => ProvinceSimulation(province)),area, from -> until)
      case region: Region => SimulationsManager[RegionSimulation]((1 to runs)
        .map(_ => RegionSimulation(region)),area, from -> until)
      case _ => SimulationsManager[NationSimulation]((1 to runs)
        .map(_ => NationSimulation()),area, from -> until)
    }
  }

  override def tick(time: Calendar): Unit = {
    places.foreach(place => place.propagateVirus(time, place)(covidInfectionParameters))
     TransportLinesContainer.getTrainLines.foreach(trainLine =>trainLine.trainList.foreach(train=> train.propagateVirus(time, train)(covidInfectionParameters)))
     TransportLinesContainer.getBusLines.foreach(busLine => busLine.busList.foreach(bus=> bus.propagateVirus(time, bus )(covidInfectionParameters)))
    if(time.hour == 0) {
      simulationsManager.takeScreenshot(time, people)
    }
  }

  override def reset(): Unit = {
    places.foreach(_.clear())
    people.foreach(_.resetState())
  }

  override def simulationsManager: SimulationsManager[Simulation] = _simulationsManager

  override def setSimulationParameters(safeZone: Double,
                                       minRecoverTime: Int, maxRecoverTime: Int,
                                       minInfectionDetectionTime: Int, maxInfectionDetectionTime: Int,
                                       multipleInfectionProbability: Double,
                                       asymptomaticProbability: Double,
                                       asymptomaticDetectionCondProbability: Double,
                                       contagionProbability: Double,
                                       minMaskProbability: Double, maxMaskProbability: Int,
                                       averageSocialDistance: Double,
                                       notRespectingIsolationMaxProbability: Double,
                                       lockDownStart: Double, lockDownEnd: Double,
                                       closedPlaceSet: Set[Class[_ <: Place]]): Unit = {
    covidInfectionParameters.safeZone = safeZone
    covidInfectionParameters.minRecoverTime = minRecoverTime
    covidInfectionParameters.maxRecoverTime = maxRecoverTime
    covidInfectionParameters.minInfectionDetectionTime = minInfectionDetectionTime
    covidInfectionParameters.maxInfectionDetectionTime = maxInfectionDetectionTime
    covidInfectionParameters.multipleInfectionProbability = multipleInfectionProbability
    //covidInfectionParameters.asymptomaticProbability = asymptomaticProbability
    covidInfectionParameters.asymptomaticDetectionCondProbability = asymptomaticDetectionCondProbability
    covidInfectionParameters.contagionProbability = contagionProbability
    covidInfectionParameters.minMaskProbability = minMaskProbability
    covidInfectionParameters.maxMaskProbability = maxMaskProbability
    covidInfectionParameters.notRespectingIsolationMaxProbability = notRespectingIsolationMaxProbability
    covidInfectionParameters.lockDownStart = lockDownStart
    covidInfectionParameters.lockDownEnd = lockDownEnd
    covidInfectionParameters.placesToClose = closedPlaceSet

  }

  private def initPeopleFriends(people: ParSeq[Person]): Unit = {
    people.foreach(p => {
      // each person can have from 2 to 10 close friends
      val friendsCount = RandomGeneration.randomIntInRange(2, 10)
      (1 to friendsCount).foreach(_ => {
        val availablePeople = people.filter(_.residence == p.residence)
        val friend: Person = availablePeople(Random.nextInt(availablePeople.size))
        if (p != friend) {
          p.addFriend(friend)
          if (Random.nextFloat() < 0.8) friend.addFriend(p) // if you add me to your friends list, I'll add you at 80%
        }
      })
    })
  }
}
