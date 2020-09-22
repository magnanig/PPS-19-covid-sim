package pps.covid_sim.model.people

import pps.covid_sim.model.clinical.CovidInfection
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.Place

import java.util.Calendar

trait Person {

  val residence: City = null

  val birthDate: Calendar = null

  val age: Int = 0 //Calendar.getInstance() -- birthDate

  private val covidInfection: Option[CovidInfection] = None

  def infectionPlaceInstance: Option[Place] = covidInfection.map(_.at)

  def infectionPlace: Option[Class[_ <: Place]] = infectionPlaceInstance.map(_.getClass)

  def friends: Set[Person] = Set()

  /**
   * Get the mask worn by current person.
   * @return  the worn mask
   */
  def wornMask: Option[Mask] = ???

  /**
   * Get the social distance that this person is keeping.
   * @return  the actual social distance, in meters
   */
  def socialDistance: Double = 0.5

  /**
   * Checks whether current person can infect other people.
   * Keep in mind that a person can be contagious without knowing
   * he is infected (i.e. he hasn't done tampon yet).
   * @return  true if current person can infect, false otherwise
   */
  def canInfect: Boolean = ???

  /**
   * Checks whether current person is been certified to be positive
   * at coronavirus test, if done.
   * @return  true if current person is positive at coronavirus test,
   *          false otherwise
   */
  def isInfected: Boolean

  /**
   * Checks whether current person has been recovered from virus.
   * @return  true if current person has been recovered from virus,
   *          false otherwise
   */
  def isRecovered: Boolean

  /**
   * Checks whether current person has been died.
   * @return  true if current person has been died, false otherwise
   */
  def isDeath: Boolean

  /**
   * Infect current person.
   * @param place   the place where infection has happened
   * @param time    the time when infection has happened
   */
  def infects(place: Place, time: Calendar): Unit = ???

  /**
   * Get the set of infected people that current person has met.
   * @return  the set of infected people met by current person
   */
  def infectedPeopleMet: Set[Person] = ???

  /**
   * Add the specified infected person to the set of infected people met.
   * @param person  the infected person to be added
   */
  def metInfectedPerson(person: Person): Unit = ???
}

