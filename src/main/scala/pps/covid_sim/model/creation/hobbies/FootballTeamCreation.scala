package pps.covid_sim.model.creation.hobbies

import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Hobbies.FootballTeam
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.OpenPlaces.Field
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.samples.Places
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[hobbies] case class FootballTeamCreation() {

  def create(city: City,
             workers: List[Worker],
             fieldsRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    var teams: List[FootballTeam] = List()
    val totalWorker: Int = workers.size
    var numWorker: Int = 0

    while (numWorker < totalWorker) {
      var fields: Set[Field] = Set()
      (1 to randomIntInRange(fieldsRange._1, fieldsRange._2, random)).foreach(_ => { // number of fields
        fields += Field(city, Places.FOOTBALL_FIELD_PRIVATE_TIME_TABLE, !closedPlaceInLockdown.contains(classOf[Field]))
      })
      val footballTeam: FootballTeam = FootballTeam(city, !closedPlaceInLockdown.contains(classOf[Field]), fields)
      for (field <- fields) {
        // number of workers (people) who will be assigned to this office
        val bound: Int = Statistic.getMin(numWorker + randomIntInRange(staffRange._1, staffRange._2, random), totalWorker)
        if (numWorker < totalWorker) {
          workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
            val plan: WorkPlan[Field] = WorkPlan(!closedPlaceInLockdown.contains(classOf[Field]))
              .add(field, Day.TUESDAY -> Day.FRIDAY, 9 -> 14)
              .commit()
            footballTeam.addWorkPlan(worker, plan)
            worker.setWorkPlace(footballTeam)
          })
        }
        numWorker = bound
      }
      teams = footballTeam :: teams
    }
    teams
  }

}
