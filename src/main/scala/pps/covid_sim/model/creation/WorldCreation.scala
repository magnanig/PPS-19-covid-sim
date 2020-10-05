package pps.covid_sim.model.creation

import pps.covid_sim.model.creation.region.RegionCreation
import pps.covid_sim.model.places.Locality.Region

import scala.collection.mutable

/**
 * It takes care of creating the entire application domain
 * based on the entire region.
 */
object WorldCreation {

  private var regions: mutable.Map[Int, Region] = mutable.Map[Int, Region]() // id_region -> Region

  /**
   * For each Italian region: cities, people and commonplaces of
   * aggregation are created.
   */
  def create(): Unit = {
    regionsCreation()
    regions.values.foreach( RegionCreation.create)
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
