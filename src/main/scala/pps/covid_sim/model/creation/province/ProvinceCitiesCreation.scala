package pps.covid_sim.model.creation.province

import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.parameters.CreationParameters

import scala.collection.mutable

private[province] object ProvinceCitiesCreation {

  /**
   * Create all the cities of a specific region.
   *
   * @param province  province whose cities will be created.
   * @return          list of all the cities that have been created.
   */
  def create(province: Province): Set[City] = { new ProvinceCitiesCreation(province).create() }

}

private class ProvinceCitiesCreation(val province: Province) {

  private var provinces: mutable.Map[String, Province] = mutable.Map[String, Province]() // province_abbreviation -> Province
  private var regions: mutable.Map[Int, Region] = mutable.Map[Int, Region]() // id_region -> Region
  var cities: Set[City] = Set[City]()

  def create(): Set[City] = {
    regionsCreation()
    provincesCreation()
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    for (line <- bufferedSource.getLines) {
      val Array(istat, name, abbreviation, _, _, _, num_residents, longitude, latitude) = line.split(";")
      if (abbreviation.equals(province.abbreviation) &&
        num_residents.toInt * CreationParameters.citizensPercentage > CreationParameters.minCitizens) {
        cities += City(istat.toInt, name, Math.round(num_residents.toInt * CreationParameters.citizensPercentage).toInt,
          provinces(abbreviation), latitude.toDouble, longitude.toDouble)
      }
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
      val Array(abbreviation, istat, name, id_region, longitude, latitude) = line.split(";")
      provinces += (abbreviation -> Province(istat.toInt, name, abbreviation, regions(id_region.toInt),
        latitude.toDouble, longitude.toDouble))
    }
    bufferedSource.close
  }

}
