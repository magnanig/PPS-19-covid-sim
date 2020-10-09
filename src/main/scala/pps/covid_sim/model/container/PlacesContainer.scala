package pps.covid_sim.model.container

import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

import scala.collection.mutable
import scala.collection.parallel.ParSeq
import scala.collection.parallel.mutable.ParMap

/**
 * Represents a singleton object as it takes care of keeping the
 * references of all the places that have been created in the whole
 * system.
 */
object PlacesContainer {

  private var _places: PlacesCreation = new PlacesCreation()

  /**
   * Delete all places created so far.
   */
  def reset(): Unit = {
    _places = new PlacesCreation()
  }

  /**
   * Adds a new associated place in a specific city
   *
   * @param city  city where the new place will be created.
   * @param place place that was created in the city.
   */
  def add(city: City, place: Place): Unit = {
    _places.add(city, place)
  }

  /**
   * It goes to find all the places that have been created in the
   * entire application domain.
   *
   * @return  list of all the places that have been created in the
   *          entire application domain.
   */
  def getPlaces: List[Place] = _places.cityMap.flatMap(_._2.planMap.flatMap(_._2)).toList

  /**
   * Finds all places of a specific type that have been created in
   * the entire application domain. For example, you can get the
   * restaurants only.
   *
   * @param placeClass  type of places you want to get.
   * @return            list of all places of a specific type that
   *                    have been created in the entire application
   *                    domain.
   */
  def getPlaces(placeClass: Class[_ <: Place]): List[Place] = _places.cityMap.toList //.toList always needed here!!
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)

  /**
   * It goes to find all the places of a specific type that are
   * present within a specific city. For example, you can only get
   * the restaurants that are present in the city of Cervia.
   *
   * @param city        city where places will be searched.
   * @param placeClass  type of places you want to get.
   * @return            list of all places of a specific type that
   *                    are present in a specific city.
   */
  def getPlaces(city: City, placeClass: Class[_ <: Place]): List[Place] = _places.cityMap.toList
    .filter(_._1 == city)
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)

  /**
   * It goes to find all the places of a specific type that are
   * present within a specific province. For example, you can only
   * get the restaurants that are present in the province of Bologna.
   *
   * @param province    province where places will be searched.
   * @param placeClass  type of places you want to get.
   * @return            list of all places of a specific type that
   *                    are present in a specific province.
   */
  def getPlaces(province: Province, placeClass: Class[_ <: Place]): List[Place] = _places.cityMap.toList
    .filter(_._1.province == province)
    .flatMap(_._2.planMap) //second map
    .filter(_._1 == placeClass)
    .flatMap(_._2)

  /**
   * It goes to find all the places of a specific type that are
   * present within a specific province and that are accessible to the
   * public at a specific time. For example, you can only get the
   * restaurants that are present in the province of Bologna and
   * that are open from 7pm to 10pm.
   *
   * @param province      province where places will be searched.
   * @param placeClass    type of places you want to get.
   * @param datesInterval opening hours of the place.
   * @return              list of all places of a specific type that
   *                      are present in a specific province and that
   *                      are accessible to the public at a specific time.
   */
  def getPlaces[T <: Place](province: Province, placeClass: Class[T], datesInterval: DatesInterval): List[T] = {
    _places.cityMap.toList
      .filter(_._1.province == province)
      .flatMap(_._2.planMap) //second map
      .filter(_._1 == placeClass)
      .flatMap(_._2)
      .map(place => place.asInstanceOf[T])
      .filter(_.isOpen(datesInterval))
  }

  /**
   * It goes to find all the places of a specific type that are
   * present within a specific city and that are accessible to the
   * public at a specific time. For example, you can only get the
   * restaurants that are present in the city of Cervia and
   * that are open from 7pm to 10pm.
   *
   * @param city          city where places will be searched.
   * @param placeClass    type of places you want to get.
   * @param datesInterval opening hours of the place.
   * @return              list of all places of a specific type that
   *                      are present in a specific city and that are
   *                      accessible to the public at a specific time.
   */
  def getPlaces[T <: Place](city: City, placeClass: Class[T], datesInterval: DatesInterval): List[T] =
    _places.cityMap(city)
      .planMap.get(placeClass)
      .map(places => places.map(_.asInstanceOf[T])
        .filter(_.isOpen(datesInterval)).toList)
      .getOrElse(List.empty)

  /**
   * It goes to find all the places of a specific type that are
   * present within a specific city and that are accessible to the
   * public at a specific time.
   *
   * @note                In case there are no places in that city, then go and
   *                      look for places in the province of that city.
   *                      For example, you can only get the restaurants that are
   *                      present in the city of Cervia and that are open from
   *                      7pm to 10pm. If there are no restaurants in the city
   *                      of Cervia, look for restaurants open from 7pm to 10pm
   *                      in the province of Ravenna.
   *
   * @param city          city where places will be searched.
   * @param placeClass    type of places you want to get.
   * @param datesInterval opening hours of the place.
   * @return              list of all places of a specific type that
   *                      are present in a specific city and that are
   *                      accessible to the public at a specific time.
   */
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
        planMap.put(placeClass, planMap(placeClass) :+ place)
      else
        planMap.put(placeClass, ParSeq(place))
      this
    }

  }

}
