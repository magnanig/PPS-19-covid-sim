package pps.covid_sim.model.people

import java.util.Calendar

import pps.covid_sim.model.clinical.CovidInfection
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.{Habitation, Place}
import pps.covid_sim.util.geometry.Coordinates
import pps.covid_sim.util.time.Time.ScalaCalendar

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

  def habitation: Habitation = _habitation


  /**
   * Get all friends.
   *
   * @return the current person's friends
   */
  def friends: Set[Person] = _friends

  def position: Coordinates = _coordinates

  def position_=(coordinates: Coordinates): Unit = {
    _coordinates = coordinates
  }

  /**
   * Get the social distance that this person is keeping.
   * @return  the actual social distance, in meters
   */
  def socialDistance: Double = _socialDistance

  def socialDistance_=(distance: Double): Unit = {
    _socialDistance = distance
  }

  def setMask(mask: Option[Mask]): Unit = {
    _wornMask = mask
  }

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

  def infectionPlaceInstance: Option[Place] = covidInfection.map(_.at)

  def infectionPlace: Option[Class[_ <: Place]] = infectionPlaceInstance.map(_.getClass)

  /**
   * Infects current person.
   * @param place   the place where infection has happened
   * @param time    the time when infection has happened
   */
  def infects(place: Place, time: Calendar): Unit = synchronized {
    covidInfection = Some(CovidInfection(time, place, this))
  }

  /**
   * Get the set of infected people that current person has met.
   * @return  the set of infected people met by current person
   */
  def infectedPeopleMet: Set[Person] = _infectedPeopleMet

  /**
   * Add the specified infected person to the set of infected people met.
   * @param person  the infected person to be added
   */
  def metInfectedPerson(person: Person): Unit = {
    _infectedPeopleMet += person
  }

  def clearInfectedPeopleMet(): Unit = {
    _infectedPeopleMet = Set.empty
  }

  override def equals(obj: Any): Boolean = obj match {
    case person: Person => this eq person
    case _ => false
  }

  private[model] def hourTick(time: Calendar): Unit = {
    if (covidInfection.isDefined) covidInfection.get.hourTick(time)
  }

  private[model] def setHabitation(habitation: Habitation): Unit = {
    _habitation = habitation
  }

  /**
   * Adds a friend to the friends list
   *
   * @param friend the friend to be added
   */
  private[model] def addFriend(friend: Person): Unit = synchronized {
    if (!_friends.contains(friend)) _friends = _friends + friend
  }

  private[model] def resetState(): Unit = {
    covidInfection = None
    clearInfectedPeopleMet()
  }
}

