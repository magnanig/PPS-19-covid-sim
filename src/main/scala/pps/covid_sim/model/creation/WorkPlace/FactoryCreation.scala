package pps.covid_sim.model.creation.WorkPlace

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Jobs.{Factory, Office}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

case class FactoryCreation() {

  def create(city: City,
             workers: List[Worker],
             officesRange: (Int, Int),
             capacityRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var factories: List[Factory] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var offices: List[Office] = List()
      (1 to randomIntInRange(officesRange._1, officesRange._2, random)).foreach(_ => { // number of offices
        offices = Office(randomIntInRange(capacityRange._1, capacityRange._2, random)) :: offices
      })
      val factory: Factory = Factory(city, offices)
      for (office <- offices) {
        // numero di lavoratori che verranno assegnate al presente ufficio
        val bound: Int = Statistic.getMin(numWorker + office.capacity, totalWorker)
        if (numWorker < totalWorker) {
          workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
            val plan: WorkPlan[Office] = WorkPlan()
              .add(office, Day.MONDAY -> Day.FRIDAY, 8 -> 12, 14 -> 18)
              .commit()
            factory.addWorkPlan(worker, plan)
            worker.setWorkPlace(factory)
          })
        }
        numWorker = bound
      }
      factories = factory :: factories
    }
    factories
  }

}
