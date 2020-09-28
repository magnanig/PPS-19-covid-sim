package pps.covid_sim.model.creation.test

import pps.covid_sim.model.places.Locality.{City, Province, Region}

import scala.collection.mutable

/**
 * Represents a singleton object, unique in the whole program.
 * Through this object you can get the whole list of cities.
 */
object CitiesObject {

  private val citiesCreation: CitiesObject = new CitiesObject()

  /**
   * When the set of all cities is requested, the composition of this list occurs only once,
   * only at the first call of the method. All subsequent calls to this method do not cause
   * the creation of a new list.
   *
   * @return a set of all cities
   */
  def getCities: Set[City] =  if (citiesCreation.cities.isEmpty) citiesCreation.create() else citiesCreation.cities

  /**
   * Get all cities that are in a specific province
   *
   * @param provinceAbbreviation abbreviation code of the province of which you want all the cities
   * @return all cities that are in a specific province
   */
  def getCities(provinceAbbreviation: String): Set[City] =
    getCities.filter(city => city.province.abbreviation.equals(provinceAbbreviation))

  /**
   * Get all cities that are in a specific province
   *
   * @param province  the province of which you want all the cities
   * @return          all cities that are in the specified province
   */
  def getCities(province: Province): Set[City] =
    getCities.filter(city => city.province == province)

  /**
   * Get all cities that are in a specific region
   *
   * @param region region of which you want all the cities
   * @return       all cities that are in a specific region
   */
  def getCities(region: Region): Set[City] =
    getProvince(region).flatMap(province => getCities(province.abbreviation))

  /**
   * Get all the provinces of a specific region
   *
   * @param region specific region whose provinces are to be get
   * @return       all the provinces of a specific region
   */
  def getProvince(region: Region): Set[Province] =
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
  def getRegions: Set[Region] = getCities.map(city => city.province.region)

}

private class CitiesObject {

  private var provinces: mutable.Map[String, Province] = mutable.Map[String, Province]() // province_abbreviation -> Province
  private var regions: mutable.Map[Int, Region] = mutable.Map[Int, Region]() // id_region -> Region
  var cities: Set[City] = Set[City]()

  def create(): Set[City] = {
    regionsCreation()
    provincesCreation()
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    for (line <- bufferedSource.getLines) {
      val Array(istat, name, abbreviation, _, _, _, num_residents) = line.split(";")
      cities += City(istat.toInt, name, num_residents.toInt, provinces(abbreviation))
    }
    bufferedSource.close
    cities
  }

  private def regionsCreation(): Unit = {
    val bufferedSource = io.Source.fromFile("res/italy_regions.csv")
    for (line <- bufferedSource.getLines) {
      val Array(id_region, name, _, num_residents, _, _) = line.split(";")
      regions += (id_region.toInt -> Region(id_region.toInt, name, num_residents.toInt))
    }
    bufferedSource.close
  }

  private def provincesCreation(): Unit = {
    val bufferedSource = io.Source.fromFile("res/italy_provinces.csv")
    for (line <- bufferedSource.getLines) {
      val Array(abbreviation, istat, name, id_region) = line.split(";")
      provinces += (abbreviation -> Province(istat.toInt, name, abbreviation, regions(id_region.toInt)))
    }
    bufferedSource.close
  }

}
