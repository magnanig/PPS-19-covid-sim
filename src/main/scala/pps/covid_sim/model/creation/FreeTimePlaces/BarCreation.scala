package pps.covid_sim.model.creation.FreeTimePlaces

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.Bar
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.rooms.TablesRoom
import pps.covid_sim.model.places.samples.Places
import pps.covid_sim.util.RandomGeneration.{randomIntFromGaussian, randomIntInRange}
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

case class BarCreation() {

  def create(city: City,
             workers: List[Worker],
             roomsRange: (Int, Int),
             capacityRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var bars: List[Bar] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var rooms: List[TablesRoom] = List()
      (1 to randomIntInRange(roomsRange._1, roomsRange._2, random)).foreach(_ => { // number of rooms
        rooms = TablesRoom(randomIntInRange(capacityRange._1, capacityRange._2, random),
          randomIntFromGaussian(3, 3, 2)) :: rooms // capacity of each restaurant table
      })
      val bar: Bar = Bar(city, Places.BAR_TIME_TABLE, rooms)
      // numero di persone lavoratrici che verranno assegnate al locale
      val bound: Int = Statistic.getMin(numWorker +
        randomIntInRange(staffRange._1 * rooms.size, staffRange._2 * rooms.size, random), totalWorker)
      workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
        val plan: WorkPlan[Bar] = WorkPlan()
          .add(bar, Day.TUESDAY -> Day.SUNDAY, 6 -> 18)
          .commit()
        bar.addWorkPlan(worker, plan)
        worker.setWorkPlace(bar)
      })
      numWorker = bound
      bars = bar :: bars
    }
    bars
  }

}
