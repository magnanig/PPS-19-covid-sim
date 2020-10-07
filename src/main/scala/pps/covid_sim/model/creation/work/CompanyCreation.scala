package pps.covid_sim.model.creation.work

import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.Bar
import pps.covid_sim.model.places.Jobs.{Company, Office}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[work] case class CompanyCreation() {

  def create(city: City,
             workers: List[Worker],
             officesRange: (Int, Int),
             capacityRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var companies: List[Company] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var offices: List[Office] = List()
      (1 to randomIntInRange(officesRange._1, officesRange._2, random)).foreach(_ => { // offices number
        offices = Office(randomIntInRange(capacityRange._1, capacityRange._2, random)) :: offices
      })
      val company: Company = Company(city, !closedPlaceInLockdown.contains(classOf[Company]), offices)
      for (office <- offices) {
        // number of workers who will be assigned to this office
        val bound: Int = Statistic.getMin(numWorker + office.capacity, totalWorker)
        if (numWorker < totalWorker) {
          workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
            val plan: WorkPlan[Office] = WorkPlan(!closedPlaceInLockdown.contains(classOf[Company]))
              .add(office, Day.MONDAY -> Day.FRIDAY, 8 -> 12, 14 -> 18)
              .commit()
            company.addWorkPlan(worker, plan)
            worker.setWorkPlace(company)
          })
        }
        numWorker = bound
      }
      companies = company :: companies
    }
    companies
  }

}
