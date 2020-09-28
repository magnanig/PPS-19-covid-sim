package pps.covid_sim.model.creation.Hobbies

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Hobbies.Gym
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.rooms.GymRoom
import pps.covid_sim.model.places.samples.Places
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

case class GymCreation() {

  def create(city: City,
             workers: List[Worker],
             roomsRange: (Int, Int),
             capacityRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var gyms: List[Gym] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var rooms: List[GymRoom] = List()
      (1 to randomIntInRange(roomsRange._1, roomsRange._2, random)).foreach(_ => { // number of rooms
        rooms = GymRoom(randomIntInRange(capacityRange._1, capacityRange._2, random)) :: rooms
      })
      val gym: Gym = Gym(city, Places.GYM_TIME_TABLE, rooms)
      for (room <- rooms) {
        // numero di lavoratori che verranno assegnati alla presente stanza di lavoro
        val bound: Int = Statistic.getMin(numWorker + randomIntInRange(staffRange._1, staffRange._2, random), totalWorker)
        if (numWorker < totalWorker) {
          workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
            val plan: WorkPlan[GymRoom] = WorkPlan()
              .add(room, Day.MONDAY -> Day.SATURDAY, 9 -> 20)
              .commit()
            gym.addWorkPlan(worker, plan)
            worker.setWorkPlace(gym)
          })
        }
        numWorker = bound
      }
      gyms = gym :: gyms
    }
    gyms
  }

}
