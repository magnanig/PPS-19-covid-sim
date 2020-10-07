package pps.covid_sim.model.creation

import pps.covid_sim.model.container.{CitiesContainer, PeopleContainer, PlacesContainer, TransportLinesContainer}
import pps.covid_sim.model.creation.province.ProvinceCreation
import pps.covid_sim.model.creation.region.RegionCreation
import pps.covid_sim.model.places.Locality.{Area, City, Italy, Province, Region}
import pps.covid_sim.model.places.Place

import scala.collection.mutable

/**
 * It takes care of creating the entire application domain
 * based on the entire region.
 */
private[model] object WorldCreation {

  private var regions: mutable.Map[Int, Region] = mutable.Map[Int, Region]() // id_region -> Region
  private[creation] var closedPlaceInLockdown: Set[Class[_ <: Place]] = _

  /**
   * Resets all data structures relating to the
   * entire application domain
   */
  def reset(): Unit = {
    CitiesContainer.reset()
    PeopleContainer.reset()
    PlacesContainer.reset()
    TransportLinesContainer.reset()
  }

  /**
   * For each Italian region: cities, people and commonplaces of
   * aggregation are created.
   */
  def create(area: Area, closedPlaceInLockdown: Set[Class[_ <: Place]]): Unit = {
    this.closedPlaceInLockdown = closedPlaceInLockdown
    area match {
      case city: City => ProvinceCreation.create(city.province)
      case province: Province => ProvinceCreation.create(province)
      case region: Region => RegionCreation.create(region)
      case Italy() =>
        regionsCreation()
        regions.values.foreach(RegionCreation.create)
    }
    //PeopleContainer.checkAssignedWork()
  }

  private def regionsCreation(): Unit = {
  val bufferedSource = io.Source.fromFile("res/italy_regions.csv")
    for (line <- bufferedSource.getLines) {
      val Array(id_region, name, _, num_residents, _, _) = line.split(";")
      regions += (id_region.toInt -> Region(id_region.toInt, name, num_residents.toInt))
    }
    bufferedSource.close
  }

}
