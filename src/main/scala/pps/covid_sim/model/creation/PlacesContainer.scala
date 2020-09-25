package pps.covid_sim.model.creation

import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.{LimitedHourAccess, Place}
import pps.covid_sim.util.time.DatesInterval

import scala.collection.mutable

object PlacesContainer {

  // TODO: add ScalaDoc

  private val places: PlacesCreation = new PlacesCreation()

  def add(city: City, place: Place): Unit = { places.add(city, place) }

  lazy val getPlaces: List[Place] = places.cityMap.flatMap(_._2.planMap.flatMap(_._2)).toList

  def getPlaces(placeClass: Class[_ <: Place]): List[Place] = places.cityMap.toList //.toList always needed here!!
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)


  def getPlaces(city: City, placeClass: Class[_ <: Place]): List[Place] = places.cityMap.toList
    .filter(_._1 == city)
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)

  def getPlaces(province: Province, placeClass: Class[_ <: Place]): List[Place] = places.cityMap.toList
    .filter(_._1.province == province)
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)

  def getPlaces(province: Province, placeClass: Class[_ <: Place], datesInterval: DatesInterval): List[Place] =
    places.cityMap.toList
      .filter(_._1.province == province)
      .flatMap(_._2.planMap) //second map
      .filter(_._1 == placeClass)
      .flatMap(_._2)
      .filter(p => (p.isInstanceOf[LimitedHourAccess] &&
        p.asInstanceOf[LimitedHourAccess].timeTable.isDefinedBetween(datesInterval)) || !p.isInstanceOf[LimitedHourAccess])

  def getPlaces(city: City, placeClass: Class[_ <: Place], datesInterval: DatesInterval): List[Place] =
    places.cityMap.toList
      .filter(_._1 == city)
      .flatMap(_._2.planMap) //second map
      .filter(_._1 == placeClass)
      .flatMap(_._2)
      .filter(p => (p.isInstanceOf[LimitedHourAccess] &&
        p.asInstanceOf[LimitedHourAccess].timeTable.isDefinedBetween(datesInterval)) || !p.isInstanceOf[LimitedHourAccess])

  def placesInCityOrElseInProvince(city: City,
                                   placeClass: Class[_ <: Place],
                                   datesInterval: DatesInterval): List[Place] = {
    PlacesContainer.getPlaces(city, placeClass, datesInterval) match {
      case Nil => PlacesContainer.getPlaces(city, placeClass, datesInterval)
      case places: List[Place] => places
    }
  }

  private class PlacesCreation() {

    val cityMap: mutable.Map[City, PlacesCreationPlan] = mutable.Map()

    def add(city: City, place: Place): Unit = {
      if (cityMap.contains(city))
        cityMap(city).add(place.getClass, place)
      else
        cityMap.put(city, new PlacesCreationPlan().add(place.getClass, place))
    }

  }

  private class PlacesCreationPlan() {

    val planMap: mutable.Map[Class[_ <: Place], mutable.ListBuffer[Place]] = mutable.Map()

    def add(placeClass: Class[_ <: Place], place: Place): PlacesCreationPlan = {
      if (planMap.contains(placeClass))
        planMap(placeClass) += place
      else
        planMap.put(placeClass, mutable.ListBuffer(place))
      this
    }

  }

}