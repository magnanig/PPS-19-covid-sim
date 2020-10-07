package pps.covid_sim.model.container

import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}

object CitiesContainer {

  private var _cities: Set[City] = Set()

  /**
   * Delete all cities created so far.
   */
  def reset(): Unit = {
    _cities = Set()
  }

  /**
   * Adds a city in the container.
   */
  def add(city: City): Unit = {
    _cities += city
  }

  /**
   * Adds a list of cities in the container.
   */
  def add(cities: Set[City]): Unit = {
    _cities ++= cities
  }

  /**
   * Get all cities
   *
   * @return  all cities in the current simulation
   */
  def getCities: Set[City] = _cities

  /**
   * Get all cities that are in a specific province.
   *
   * @param provinceAbbreviation abbreviation code of the province of which you want all the cities
   * @return all cities that are in a specific province.
   */
  def getCities(provinceAbbreviation: String): Set[City] =
    getCities.filter(city => city.province.abbreviation.equals(provinceAbbreviation))

  /**
   * Get all cities that are in a specific area
   *
   * @param area  the area of which you want all the cities
   * @return      all cities that are in the specified area
   */
  def getCities(area: Area): Set[City] = area match {
    case province: Province => getCities(province)
    case region: Region => getCities(region)
    case city: City => Set(city)
    case _ => getCities
  }

  private def getCities(province: Province): Set[City] =
    getCities.filter(city => city.province == province)

  private def getCities(region: Region): Set[City] =
    getProvince(region).flatMap(province => getCities(province.abbreviation))

  /**
   * Get all the provinces of a specific region
   *
   * @param region specific region whose provinces are to be get
   * @return all the provinces of a specific region
   */
  def getProvince(region: Region): Set[Province] =
    getCities.filter(city => city.province.region.equals(region)).map(city => city.province)

  /**
   * Get the Province object associated with the input province
   *
   * @param province abbreviated name of the province. For example, if you want
   *                 to get the object of the province of "Ravenna", in input you
   *                 must specify "RA"
   * @return the Province object associated with the input province
   */
  def getProvince(province: String): Province =
    getCities.collectFirst({ case city: City if city.province.abbreviation.equals(province) => city.province }).get

  /**
   * Get all regions of the country
   *
   * @return all regions of the country
   */
  def getRegions: Set[Region] = getCities.map(city => city.province.region)

}
