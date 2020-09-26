package pps.covid_sim.model.creation

import scala.util.Random

import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.people.People.Worker

//TODO scalaDoc
case class HobbyPlacesCreation() {

  def create(city: City,
             workerPerHobbyPlace: List[Int],
             workers: List[Worker],
             random: Random = new Random()): List[Place] = {

    var hobbyPlaces: List[Place] = List()
    var index: Int = 0

    hobbyPlaces = hobbyPlaces ::: FootballTeamCreation().create(city,
      workers.slice(index, index + workerPerHobbyPlace.head - 1),
      fieldsRange = (2, 3),
      staffRange = (25, 42), random)
    index += workerPerHobbyPlace.head

    hobbyPlaces = hobbyPlaces ::: GymCreation().create(city,
      workers.slice(index, index + workerPerHobbyPlace.last - 1),
      roomsRange = (2, 6),
      capacityRange = (20, 90),
      staffRange = (4, 10), random)

    hobbyPlaces
  }

}
