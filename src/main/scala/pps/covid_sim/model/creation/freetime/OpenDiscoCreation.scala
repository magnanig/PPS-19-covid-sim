package pps.covid_sim.model.creation.freetime

import pps.covid_sim.model.Statistic
import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.OpenDisco
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.samples.Places
import pps.covid_sim.model.scheduling.Planning.WorkPlan
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[freetime] case class OpenDiscoCreation() {

  def create(city: City,
             workers: List[Worker],
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var openDiscos: List[OpenDisco] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      val openDisco: OpenDisco = OpenDisco(city, Places.DISCO_TIME_TABLE, !closedPlaceInLockdown.contains(classOf[OpenDisco]))
      // number of workers (people) who will be assigned to the open disco
      val bound: Int = Statistic.getMin(numWorker +
        randomIntInRange(staffRange._1, staffRange._2, random), totalWorker)
      workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
        val plan: WorkPlan[OpenDisco] = WorkPlan(!closedPlaceInLockdown.contains(classOf[OpenDisco]))
          .add(openDisco, Day.FRIDAY -> Day.SATURDAY, 22 -> 6)
          .commit()
        openDisco.addWorkPlan(worker, plan)
        worker.setWorkPlace(openDisco)
      })
      numWorker = bound
      openDiscos = openDisco :: openDiscos
    }
    openDiscos
  }

}
