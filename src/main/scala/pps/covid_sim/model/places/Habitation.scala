package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Dimension
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.util.Random

/**
 * A generic habitation for multiple people.
 * @param city            the city where habitation is located
 * @param leaderElection  a function to elect a leader, in charge of doing commissions.
 *                        If not specified, the leader is elected by choosing a random
 *                        person with at least 18 years old. If nobody is found, then
 *                        leader will be the oldest person in the habitation.
 */
case class Habitation(override val city: City,
                      leaderElection: Set[Person] => Person = people => Random.shuffle(people)
                       .find(_.age >= 18)
                       .getOrElse(people.maxBy(_.age))) extends ClosedPlace with Iterable[Person] {
  val dimension: Dimension = (
    RandomGeneration.randomIntInRange(10, 20),
    RandomGeneration.randomIntInRange(10, 20)
  )

  private val sleepingHours = HoursInterval(0, 8)
  private var members: Set[Person] = Set()

  /**
   * The habitation's leader, in charge of doing commissions. If leader hasn't been elected yet
   * (i.e. by accessing this property), it will be elected now.
   */
  lazy val leader: Person = leaderElection(members)

  /**
   * Add a person to current habitation
   * @param person  the person to be added
   */
  def addMember(person: Person): Unit = {
    members += person
  }

  /**
   * If people aren't sleeping, try to infect each pair of people.
   * @param time    current time
   * @param place   current place
   */
  override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    val socialDistance = 0.5
    if (!sleepingHours.contains(time.hour)) currentGroups
      .flatMap(_.people)
      .toList
      .combinations(2)
      .foreach(pair => VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time)(socialDistance))
  }

  override def mask: Option[Mask] = None

  override def iterator: Iterator[Person] = members.iterator
}
