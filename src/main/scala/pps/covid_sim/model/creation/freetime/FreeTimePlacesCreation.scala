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
      roomsRange = (2, 4),  // sempre ok in ogni caso

      //TODO scegliere un numero casuale tra 15 e 90 e POI applicare il fattore di scala (con limite minimo sul risultato)
      capacityRange = (15, 90), // TODO: diventa capacity = Math.max(minCapacity, (Random(15, 90) * CreationParameters.citizensPercentage))

      // TODO modificare il modo in cui si calcola staffRange, ovvero definirlo in funzione della capacity ottenuta al
      //  punto precedente. Ad esempio, fare che lo staff è il X% (?) della capienza
      //  (NOTA: ogni posto avrà una % diversa a seconda del tipo di posto ---> pensare alla % più plausible per ogni posto)
      staffRange = (2, 8), random) // TODO: diventa staff = Math.max(minStaff, capacity * X)
    index += workerPerFreeTimePlace.head

    // TODO: ripetere le modifiche ovunque in ogni punto possibile immaginabile della creazione ahahah

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
      roomsRange = (2, 6),
      capacityRange = (20, 180),
      staffRange = (6, 16), random)
    index += workerPerFreeTimePlace(3)

    freeTimePlaces = freeTimePlaces ::: OpenDiscoCreation().create(city,
      worker.slice(index, index + workerPerFreeTimePlace.last),
      staffRange = (20, 60), random) // TODO: così tante persone nello staff?

    freeTimePlaces
  }

}
