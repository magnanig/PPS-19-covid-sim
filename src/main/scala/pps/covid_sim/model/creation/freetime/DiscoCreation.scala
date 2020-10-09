package pps.covid_sim.model.creation.freetime

import pps.covid_sim.model.Statistic
import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.Disco
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.rooms.DiscoRoom
import pps.covid_sim.model.samples.Places
import pps.covid_sim.model.scheduling.Planning.WorkPlan
import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[freetime] case class DiscoCreation() {

  def create(city: City,
             workers: List[Worker],
             roomsRange: (Int, Int),
             capacityRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var discos: List[Disco] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var rooms: List[DiscoRoom] = List()

      (1 to randomIntInRange(roomsRange._1, roomsRange._2, random)).foreach(_ => { // number of rooms
        val capacity: Int = randomIntInRange(capacityRange._1, capacityRange._2, random)
        rooms = DiscoRoom(Math.max(10, Math.round(capacity * CreationParameters.citizensPercentage).toInt)) :: rooms
      })
      //
      val disco: Disco = Disco(city, Places.DISCO_TIME_TABLE, !closedPlaceInLockdown.contains(classOf[Disco]), rooms)
      // number of workers (people) who will be assigned to the disco
      val bound: Int = Statistic.getMin(numWorker +
        randomIntInRange(staffRange._1 * rooms.size, staffRange._2 * rooms.size, random), totalWorker)
      workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
        val plan: WorkPlan[Disco] = WorkPlan(!closedPlaceInLockdown.contains(classOf[Disco]))
          .add(disco, Day.FRIDAY -> Day.SATURDAY, 22 -> 6)
          .commit()
        disco.addWorkPlan(worker, plan)
        worker.setWorkPlace(disco)
      })
      numWorker = bound
      discos = disco :: discos
    }
    discos
  }

}
