package pps.covid_sim.model.people

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place

case class PersonTmp(override val birthDate: Calendar,
                     override val residence: City,
                     infected: Boolean = false,
                     recovered: Boolean = false,
                     death: Boolean = false) extends Person {

  override def isRecovered: Boolean = recovered

  override def isInfected: Boolean = infected

  override def isDeath: Boolean = death

  /**
   * Get the mask worn by current person.
   *
   * @return the worn mask
   */
  override def wornMask: Option[Masks.Mask] = ???

  /**
   * Checks whether current person can infect other people.
   * Keep in mind that a person can be contagious without knowing
   * he is infected (i.e. he hasn't done tampon yet).
   *
   * @return true if current person can infect, false otherwise
   */
  override def canInfect: Boolean = ???

  /**
   * Infect current person.
   *
   * @param place the place where infection has happened
   * @param time  the time when infection has happened
   */
override def infects(place: Place, time: Calendar): Unit = ???

  /**
   * Get the set of infected people that current person has met.
   *
   * @return the set of infected people met by current person
   */
  override def infectedPeopleMet: Set[Person] = ???

  /**
   * Add the specified infected person to the set of infected people met.
   *
   * @param person the infected person to be added
   */
  override def metInfectedPerson(person: Person): Unit = ???
}