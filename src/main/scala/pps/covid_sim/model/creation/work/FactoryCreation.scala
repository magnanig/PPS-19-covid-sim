package pps.covid_sim.model.creation.work

import pps.covid_sim.model.Statistic
import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Jobs.{Factory, Office}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.scheduling.Planning.WorkPlan
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[work] case class FactoryCreation() {

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
      val factory: Factory = Factory(city, !closedPlaceInLockdown.contains(classOf[Factory]), offices)
      for (office <- offices) {
        // number of workers (people) who will be assigned to the office
        val bound: Int = Statistic.getMin(numWorker + office.capacity, totalWorker)
        if (numWorker < totalWorker) {
          workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
            val plan: WorkPlan[Office] = WorkPlan(!closedPlaceInLockdown.contains(classOf[Factory]))
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
