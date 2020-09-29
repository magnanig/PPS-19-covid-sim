package pps.covid_sim.model.container

import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

import scala.collection.mutable
import scala.collection.parallel.ParSeq
import scala.collection.parallel.mutable.ParMap

// TODO: add ScalaDoc
object PlacesContainer {

  private val places: PlacesCreation = new PlacesCreation()

  def add(city: City, place: Place): Unit = {
    places.add(city, place)
  }

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

  def getPlaces[T <: Place](province: Province, placeClass: Class[T], datesInterval: DatesInterval): List[T] = {
    places.cityMap.toList
      .filter(_._1.province == province)
      .flatMap(_._2.planMap) //second map
      .filter(_._1 == placeClass)
      .flatMap(_._2)
      .map(place => place.asInstanceOf[T])
      .filter(_.isOpen(datesInterval))
  }

  def getPlaces[T <: Place](city: City, placeClass: Class[T], datesInterval: DatesInterval): List[T] =
    places.cityMap(city)
      .planMap.get(placeClass)
      .map(places => places.map(_.asInstanceOf[T])
        .filter(_.isOpen(datesInterval)).toList)
      .getOrElse(List.empty)

  def placesInCityOrElseInProvince[T <: Place](city: City,
                                               placeClass: Class[T],
                                               datesInterval: DatesInterval): List[T] = {
    PlacesContainer.getPlaces(city, placeClass, datesInterval) match {
      case Nil => PlacesContainer.getPlaces(city.province, placeClass, datesInterval)
      case places: List[T] => places
    }
  }


  private class PlacesCreation() {

    val cityMap: ParMap[City, PlacesCreationPlan] = mutable.Map[City, PlacesCreationPlan]().par

    def add(city: City, place: Place): Unit = {
      if (cityMap.contains(city))
        cityMap(city).add(place.getClass, place)
      else
        cityMap.put(city, new PlacesCreationPlan().add(place.getClass, place))
    }

  }

  private class PlacesCreationPlan() {

    val planMap: ParMap[Class[_ <: Place], ParSeq[Place]] = mutable.Map().par

    def add(placeClass: Class[_ <: Place], place: Place): PlacesCreationPlan = {
      if (planMap.contains(placeClass))
        planMap(placeClass) :+ place
      else
        planMap.put(placeClass, ParSeq(place).par)
      this
    }

  }

}
