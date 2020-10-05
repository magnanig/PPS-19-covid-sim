package pps.covid_sim.model.clinical

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place

trait Infection {

  /**
   * The person who contracted the infection.
   */
  val person: Person

  /**
   * When infection has been happened.
   */
  val from: Calendar

  /**
   * At what Location infection has been happened.
   */
  val at: Location

  /**
   * Verify whether patient knows to be infected or not.
   * @return    true if the infection has been detected, false otherwise
   */
  def infectionKnown: Boolean

  /**
   * Verify whether patient has been recovered or not.
   * @return    true if patiet has been recovered, false otherwise
   */
  def isRecovered: Boolean

  /**
   * Register that a new hour has been started.
   * @param time    the current time
   */
  def hourTick(time: Calendar): Unit

}
