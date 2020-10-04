package pps.covid_sim.model.creation.freetime

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.Pub
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.samples.Places
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[freetime] case class PubCreation() {

  def create(city: City, workers: List[Worker], staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var pubs: List[Pub] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      val pub: Pub = Pub(city, Places.PUB_TIME_TABLE)
      // numero di persone lavoratrici che verranno assegnate al locale
      val bound: Int = Statistic.getMin(numWorker +
        randomIntInRange(staffRange._1, staffRange._2, random), totalWorker)
      workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
        val plan: WorkPlan[Pub] = WorkPlan()
          .add(pub, Day.THURSDAY -> Day.SUNDAY, 18 -> 2)
          .commit()
        pub.addWorkPlan(worker, plan)
        worker.setWorkPlace(pub)
      })
      numWorker = bound
      pubs = pub :: pubs
    }
    pubs
  }

}
