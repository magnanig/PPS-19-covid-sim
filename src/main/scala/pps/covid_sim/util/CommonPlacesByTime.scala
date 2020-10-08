package pps.covid_sim.util

import java.util.Calendar

import pps.covid_sim.model.places.FreeTime._
import pps.covid_sim.model.places.OpenPlaces.{Field, Park, Square}
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.Time.Day.{Day, FRIDAY, SATURDAY, SUNDAY}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.TimeIntervalsImplicits._
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, HoursInterval}

import scala.util.Random

object CommonPlacesByTime {

  private val places: Map[Day, Map[HoursInterval, Seq[Class[_ <: Place]]]] = {
    var map: Map[Day, Map[HoursInterval, Seq[Class[_ <: Place]]]] = DaysInterval.ALL_WEEK.map(day => {
      (day, Map[HoursInterval, Seq[Class[_ <: Place]]](
        (8 -> 23) -> Seq(classOf[Bar]),
        (6 -> 23) -> Seq(classOf[Square], classOf[Park]),
        (9 -> 22) -> Seq(classOf[Field]),
        (12 -> 14) -> Seq(classOf[Restaurant]),
        (18 -> 22) -> Seq(classOf[Restaurant]),
        (21 -> 4) -> Seq(classOf[Pub])
      ))
    }).toMap
    DaysInterval(FRIDAY, SATURDAY).foreach(day => {
      map = map + ((day, map(day) + ((22 -> 0) -> Seq(classOf[Disco], classOf[OpenDisco]))))
    })
    DaysInterval(SATURDAY, SUNDAY).foreach(day => {
      map = map + ((day, map(day) + ((0 -> 6) -> Seq(classOf[Disco], classOf[OpenDisco]))))
    })
    map
  }

  def availablePlaces(datesInterval: DatesInterval): Seq[Class[_ <: Place]] = datesInterval
    .flatMap(t => places(t.day).filter(_._1.contains(t.hour)).values.flatten.toSet)
    .toSeq

  def randomPlaceWithPreferences(placesPreferences: Map[Class[_ <: Place], Double],
                                 interval: DatesInterval): Option[Class[_ <: Place]] = availablePlaces(interval)
    .map(place => place -> placesPreferences.getOrElse(place, 0.0)).toMap[Class[_ <: Place], Double] match {
    case preferences if preferences.nonEmpty =>
      val total = preferences.values.sum
      val normalizedPreferences = preferences.mapValues(_ / total)
      val probability = Math.random * normalizedPreferences.values.sum // weighted probability
      normalizedPreferences
        .drop(1)
        .scanLeft(normalizedPreferences.head)((acc, cur) => (cur._1, acc._2 + cur._2))
        .collectFirst { case (place, preference) if probability <= preference => place }
    case _ => None
  }

  def randomPlace(time: Calendar): Option[Class[_ <: Place]] = Random.shuffle(availablePlaces(time)).headOption

}
