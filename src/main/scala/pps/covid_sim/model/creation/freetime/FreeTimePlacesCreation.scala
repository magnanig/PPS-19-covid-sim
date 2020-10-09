package pps.covid_sim.model.creation.freetime

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place

import scala.util.Random

private[creation] case class FreeTimePlacesCreation() {

  def create(city: City, workerPerFreeTimePlace: List[Int], worker: List[Worker],
             random: Random = new Random()): List[Place] = {

    var freeTimePlaces: List[Place] = List()
    var index: Int = 0

    freeTimePlaces = freeTimePlaces ::: RestaurantCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace.head),
      roomsRange = (2, 4),
      capacityRange = (15, 90),
      staffRange = (2, 8), random)
    index += workerPerFreeTimePlace.head

    freeTimePlaces = freeTimePlaces ::: BarCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace(1)),
      roomsRange = (1, 2),
      capacityRange = (15, 40),
      staffRange = (2, 6), random)
    index += workerPerFreeTimePlace(1)

    freeTimePlaces = freeTimePlaces ::: PubCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace(2)),
      staffRange = (6, 20), random)
    index += workerPerFreeTimePlace(2)

    freeTimePlaces = freeTimePlaces ::: DiscoCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace(3)),
      roomsRange = (1,1), //(2, 4),
      capacityRange = (20, 180),
      staffRange = (4, 10), random)
    index += workerPerFreeTimePlace(3)

    freeTimePlaces = freeTimePlaces ::: OpenDiscoCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace.last),
      staffRange = (10, 40), random)

    freeTimePlaces
  }

}
