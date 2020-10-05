package pps.covid_sim.model.clinical

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.util.Random

case class CovidInfection(override val from: Calendar,
                          override val at: Location,
                          stage: Int,
                          covidInfectionParameters: CovidInfectionParameters,
                          override val person: Person) extends Infection {

  /**
   * Check whether patient is asymptomatic or not.
   */
  val isAsymptomatic: Boolean = Random.nextDouble() < covidInfectionParameters.asymptomaticProbability(person.age)

  private val infectionDetectionTime: Int = RandomGeneration.randomIntInRange(covidInfectionParameters.minInfectionDetectionTime,
    covidInfectionParameters.maxInfectionDetectionTime)

  private val recoverTime: Int = RandomGeneration.randomIntInRange(covidInfectionParameters.minRecoverTime,
    covidInfectionParameters.maxRecoverTime)

  private val canBeDetected: Boolean = !isAsymptomatic ||
    (isAsymptomatic && Random.nextDouble() < covidInfectionParameters.asymptomaticDetectionCondProbability)

  private var _infectionKnown: Boolean = false

  private var _isRecovered: Boolean = false

  override def infectionKnown: Boolean = _infectionKnown

  override def isRecovered: Boolean = _isRecovered

  override def hourTick(time: Calendar): Unit = {
    if(!_infectionKnown && canBeDetected && time \ from >= infectionDetectionTime) {
      _infectionKnown = true
    }
    if(!_isRecovered && time \ from >= recoverTime) {
      _isRecovered = true
    }
  }

}
