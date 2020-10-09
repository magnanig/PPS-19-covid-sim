package pps.covid_sim.model.people

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.CovidInfection
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.Habitation
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.geometry.Coordinates
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.util.Random

trait Person {

  private var _habitation: Habitation = _
  private var _friends: Set[Person] = Set()
  private var _coordinates: Coordinates = (0, 0)
  private var _infectedPeopleMet: Set[Person] = Set()
  private var covidInfection: Option[CovidInfection] = None
  private var _wornMask: Option[Mask] = None
  private var _socialDistance: Double = 0.5

  val residence: City
  val birthDate: Calendar
  lazy val age: Int = Calendar.getInstance() -- birthDate

  /**
   * Get the habitation where person lives.
   * @return  the habitation of current person
   */
  def habitation: Habitation = _habitation

  /**
   * Get all friends.
   *
   * @return the current person's friends
   */
  def friends: Set[Person] = _friends

  /**
   * Get the position of person in the current space.
   * @return  the person position
   */
  def position: Coordinates = _coordinates

  /**
   * Get the social distance that this person is keeping.
   * @return  the actual social distance, in meters
   */
  def socialDistance: Double = _socialDistance

  /**
   * Get the mask worn by current person.
   * @return  the worn mask
   */
  def wornMask: Option[Mask] = _wornMask

  /**
   * Checks whether current person can infect other people.
   * Keep in mind that a person can be contagious without knowing
   * he is infected (i.e. he hasn't done tampon yet).
   * @return  true if current person can infect, false otherwise
   */
  def canInfect: Boolean = covidInfection.isDefined && !covidInfection.get.isRecovered

  /**
   * Checks whether current person is been certified to be positive
   * at coronavirus test, if done.
   * @return  true if current person is positive at coronavirus test,
   *          false otherwise
   */
  def isInfected: Boolean = covidInfection.exists(_.infectionKnown) && !covidInfection.get.isRecovered

  /**
   * Checks whether current person has been recovered from virus.
   * @return  true if current person has been recovered from virus,
   *          false otherwise
   */
  def isRecovered: Boolean = covidInfection.exists(_.isRecovered)

  /**
   * Checks whether current person has been died.
   * @return  true if current person has been died, false otherwise
   */
  def isDeath: Boolean = false

  /**
   * Get the type of place where person got infected.
   * @return  the optional type of place where person got infected
   */
  def infectionPlaceClass: Option[Location] = covidInfection.map(_.at)

  /**
   * Get the specific place where person got infected.
   * @return  the optional place where person got infected
   */
  def infectionPlace: Option[Class[_ <: Location]] = infectionPlaceClass.map(_.getClass)

  /**
   * Get the covid stage, taken by current person.
   * @return  the covid stage held by current person
   */
  def covidStage: Option[Int] = covidInfection.map(_.stage)

  /**
   * Check whether current person can be infected, considering multiple infection
   * probability.
   * @param multipleInfectionProbability  the probability of multiple infections
   * @return
   */
  def canBeInfected(multipleInfectionProbability: Double): Boolean = covidInfection.isEmpty ||
      (isRecovered && Random.nextDouble() < multipleInfectionProbability)

  override def equals(obj: Any): Boolean = obj match {
    case person: Person => this eq person
    case _ => false
  }

  /**
   * Adds a friend to the friends list
   *
   * @param friend the friend to be added
   */
  private[model] def addFriend(friend: Person): Unit = synchronized {
    if (!_friends.contains(friend)) _friends = _friends + friend
  }

  /**
   * Infects current person.
   * @param place   the place where infection has happened
   * @param time    the time when infection has happened
   */
  private[model] def infects(place: Location, time: Calendar, stage: Int)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    synchronized {
      covidInfection = Some(CovidInfection(time, place, stage, covidInfectionParameters, this))
    }
  }

  private[model] def hourTick(time: Calendar): Unit = {
    if (covidInfection.isDefined) covidInfection.get.hourTick(time)
  }

  private[model] def position_=(coordinates: Coordinates): Unit = {
    _coordinates = coordinates
  }

  private[model] def socialDistance_=(distance: Double): Unit = {
    _socialDistance = distance
  }

  private[model] def setMask(mask: Option[Mask]): Unit = {
    _wornMask = mask
  }

  private[model] def setHabitation(habitation: Habitation): Unit = {
    _habitation = habitation
  }

  private[model] def resetState(): Unit = {
    covidInfection = None
  }
}

