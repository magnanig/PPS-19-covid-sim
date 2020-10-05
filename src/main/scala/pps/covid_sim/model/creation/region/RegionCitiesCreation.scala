package pps.covid_sim.model.creation.region

import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.parameters.CreationParameters

import scala.collection.mutable

private[region] object RegionCitiesCreation {

  /**
   * Create all the cities of a specific region.
   *
   * @param region  region whose cities will be created.
   * @return        list of all the cities that have been created.
   */
  def create(region: Region): Set[City] = { new RegionCitiesCreation(region).create() }

}

private class RegionCitiesCreation(val region: Region) {

  private var provinces: mutable.Map[String, Province] = mutable.Map[String, Province]() // province_abbreviation -> Province
  var cities: Set[City] = Set[City]()

  def create(): Set[City] = {
    provincesCreation()
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    for (line <- bufferedSource.getLines) {
      val Array(istat, name, abbreviation, region_name, _, _, num_residents, longitude, latitude) = line.split(";")
      if (region.name.equals(region_name) &&
        num_residents.toInt * CreationParameters.citizensPercentage > CreationParameters.minCitizens) {
        cities += City(istat.toInt, name,
          Math.round(num_residents.toInt * CreationParameters.citizensPercentage).toInt, provinces(abbreviation),
          latitude.toDouble, longitude.toDouble)
      }
    }
    bufferedSource.close
    cities
  }

  private def provincesCreation(): Unit = {
    val bufferedSource = io.Source.fromFile("res/italy_provinces.csv")
    for (line <- bufferedSource.getLines) {
      val Array(abbreviation, istat, name, id_region, longitude, latitude) = line.split(";")
      if (region.id == id_region.toInt) {
        provinces += (abbreviation -> Province(istat.toInt, name, abbreviation, region,
          latitude.toDouble, longitude.toDouble))
      }
    }
    bufferedSource.close
  }

}
