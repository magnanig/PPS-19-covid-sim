package pps.covid_sim.model.creation.region

import pps.covid_sim.model.places.Locality.Region

/**
 * It takes care of creating the entire application domain
 * based on a specific region.
 */
object RegionCreation {

  private var created: Boolean = false

  /**
   * Create all the entities of the domain for the entire region.
   */
  def create(region: Region): Unit = {
    if (!created) {
      RegionPlacesCreation.create(region)
      RegionLinesCreation.create(region)
      created = true
    }
  }

}
