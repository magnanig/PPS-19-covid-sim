package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Locations.Location

import scala.collection.SortedMap

trait Simulation {

  /**
   * The area whose simulation refers to.
   */
  val area: Area

  /**
   * Get the number of infections happened in each location type.
   * @return  the number of infections happened in each location type
   */
  def infectionPlaces: Map[Class[_ <: Location], Int]

  /**
   * Get a sorted map with number of infected people for each day.
   * @return  a sorted map with number of infection
   */
  def infected: SortedMap[Calendar, Int]

  /**
   * Get a sorted map with number of recovered people for each day.
   * @return  a sorted map with number of recovered people
   */
  def recovered: SortedMap[Calendar, Int]

  /**
   * Get a sorted map with number of deaths for each day.
   * @return  a sorted map with number of deaths
   */
  def deaths: SortedMap[Calendar, Int]

  /**
   * Take a screenshot for the specified time, saving the number of
   * infection, recovered people and deaths.
   * @param time  current time
   */
  private[model] def takeScreenshot(time: Calendar): Unit

  /**
   * Close current simulation, saving main information.
   */
  private[model] def close(): Unit

}
