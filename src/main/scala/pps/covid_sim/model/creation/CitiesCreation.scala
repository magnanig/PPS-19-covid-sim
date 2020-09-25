package pps.covid_sim.model.creation

import pps.covid_sim.model.places.Locality.{City, Province, Region}

import scala.collection.mutable

/**
 * Represents a singleton object, unique in the whole program.
 * Through this object you can get the whole list of cities.
 */
object CitiesCreation {

  private var cities: Set[City] = Set()

  /**
   * If this method is invoked multiple times, cities are created
   * only once, only on the first call of the method. All
   * subsequent calls to this method do not cause new creations.
   *
   * @return a set of all cities
   */
  def create(region: Region): Set[City] = { if (cities.isEmpty) cities = new CitiesCreation(region).create()
    cities
  }

  def getCities: Set[City] =  cities

  /**
   * Get all cities that are in a specific province
   *
   * @param provinceAbbreviation  abbreviation code of the province
   *                              of which you want all the cities
   * @return                      all cities that are in a specific province
   */
  def getCities(provinceAbbreviation: String): Set[City] =
    cities.filter(_.province.abbreviation.equals(provinceAbbreviation))

  /**
   * Get all the provinces of this region
   *
   * @return all the provinces
   */
  def getProvinces: Set[Province] = cities.map(_.province)

  /**
   * Get the Province object associated with the input province
   *
   * @param province abbreviated name of the province. For example, if you want
   *                 to get the object of the province of "Ravenna", in input you
   *                 must specify "RA"
   * @return         the Province object associated with the input province
   */
  def getProvince(province: String): Province = getProvinces.filter(_.abbreviation.equals(province)).head

}

private class CitiesCreation(val region: Region) {

  private var provinces: mutable.Map[String, Province] = mutable.Map[String, Province]() // province_abbreviation -> Province
  var cities: Set[City] = Set[City]()

  def create(): Set[City] = {
    provincesCreation()
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    for (line <- bufferedSource.getLines) {
      val Array(istat, name, abbreviation, region_name, _, _, num_residents) = line.split(";")
      if (region.name.equals(region_name)) {
        cities += City(istat.toInt, name, num_residents.toInt, provinces(abbreviation))
      }
    }
    bufferedSource.close
    cities
  }

  private def provincesCreation(): Unit = {
    val bufferedSource = io.Source.fromFile("res/italy_provinces.csv")
    for (line <- bufferedSource.getLines) {
      val Array(abbreviation, istat, name, id_region) = line.split(";")
      if (region.id == id_region.toInt) {
        provinces += (abbreviation -> Province(istat.toInt, name, abbreviation, region))
      }
    }
    bufferedSource.close
  }

}
