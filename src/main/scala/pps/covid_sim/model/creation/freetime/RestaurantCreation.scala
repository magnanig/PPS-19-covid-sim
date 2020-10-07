package pps.covid_sim.model.creation.freetime

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.Restaurant
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.rooms.TablesRoom
import pps.covid_sim.model.samples.Places
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[freetime] case class RestaurantCreation() {

  def create(city: City,
             workers: List[Worker],
             roomsRange: (Int, Int),
             capacityRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var restaurants: List[Restaurant] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var rooms: List[TablesRoom] = List()
      (1 to randomIntInRange(roomsRange._1, roomsRange._2, random)).foreach(_ => { // number of rooms
        rooms = TablesRoom(randomIntInRange(capacityRange._1, capacityRange._2, random),
          randomIntInRange(2, 12, random)) :: rooms // capacity of each restaurant tables
      })
      val restaurant: Restaurant = Restaurant(city, Places.RESTAURANT_TIME_TABLE, rooms)
      // number of workers (people) who will be assigned to the room
      val bound: Int = Statistic.getMin(numWorker +
        randomIntInRange(staffRange._1 * rooms.size, staffRange._2 * rooms.size, random), totalWorker)
      workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
        val plan: WorkPlan[Restaurant] = WorkPlan()
          .add(restaurant, Day.TUESDAY -> Day.SUNDAY, 11 -> 14, 19 -> 22)
          .commit()
        restaurant.addWorkPlan(worker, plan)
        worker.setWorkPlace(restaurant)
      })
      numWorker = bound
      restaurants = restaurant :: restaurants
    }
    restaurants
  }

}
