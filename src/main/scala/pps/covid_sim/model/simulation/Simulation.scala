package pps.covid_sim.model.simulation

import java.util.Calendar

import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place

import scala.collection.SortedMap

trait Simulation {

  val area: Area

  def infectionPlaces: Map[Class[_ <: Location], Int]

  def infected: SortedMap[Calendar, Int]

  def recovered: SortedMap[Calendar, Int]

  def deaths: SortedMap[Calendar, Int]

  def takeScreenshot(time: Calendar): Unit

  def close(): Unit

}
