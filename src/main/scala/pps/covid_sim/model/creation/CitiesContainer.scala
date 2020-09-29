package pps.covid_sim.model.creation

import pps.covid_sim.model.places.Locality.{City, Province, Region}

object CitiesContainer {

  private var _cities: List[City] = List()

  def add(city: City): Unit = { _cities = city :: _cities }

  def add(cities: List[City]): Unit = { _cities = _cities ::: cities }

  def getCities: List[City] = _cities

  /**
   * Get all cities that are in a specific province
   *
   * @param provinceAbbreviation abbreviation code of the province of which you want all the cities
   * @return all cities that are in a specific province
   */
  def getCities(provinceAbbreviation: String): List[City] =
    getCities.filter(city => city.province.abbreviation.equals(provinceAbbreviation))

  /**
   * Get all cities that are in a specific province
   *
   * @param province  the province of which you want all the cities
   * @return          all cities that are in the specified province
   */
  def getCities(province: Province): List[City] =
    getCities.filter(city => city.province == province)

  /**
   * Get all cities that are in a specific region
   *
   * @param region region of which you want all the cities
   * @return       all cities that are in a specific region
   */
  def getCities(region: Region): List[City] =
    getProvince(region).flatMap(province => getCities(province.abbreviation))

  /**
   * Get all the provinces of a specific region
   *
   * @param region specific region whose provinces are to be get
   * @return       all the provinces of a specific region
   */
  def getProvince(region: Region): List[Province] =
    getCities.filter(city => city.province.region.equals(region)).map(city => city.province)

  /**
   * Get the Province object associated with the input province
   *
   * @param province abbreviated name of the province. For example, if you want
   *                 to get the object of the province of "Ravenna", in input you
   *                 must specify "RA"
   * @return         the Province object associated with the input province
   */
  def getProvince(province: String): Province =
    getCities.collectFirst({ case city: City if city.province.abbreviation.equals(province) => city.province }).get

  /**
   * Get all regions of the country
   *
   * @return all regions of the country
   */
  def getRegions: List[Region] = getCities.map(city => city.province.region)

}
