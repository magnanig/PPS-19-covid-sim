package pps.covid_sim.model.creation

import pps.covid_sim.model.places.Locality.City

object CitiesContainer {

  private var _cities: List[City] = List()

  def add(city: City): Unit = { _cities = city :: _cities }

  def add(cities: List[City]): Unit = { _cities = _cities ::: cities }

  def getCities: List[City] = _cities

}
