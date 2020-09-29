package pps.covid_sim.model.creation

import pps.covid_sim.model.places.Locality.{City, Province, Region}

import scala.collection.mutable

/**
 * Represents a singleton object, unique in the whole program.
 * Through this object it is possible to create all the cities
 * of a specific region.
 */
object RegionCitiesCreation {

  def create(region: Region): Set[City] = { new RegionCitiesCreation(region).create() }

}

private class RegionCitiesCreation(val region: Region) {

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
