package pps.covid_sim.model.creation.province

import pps.covid_sim.model.places.Locality.Province

/**
 * It takes care of creating the entire application domain
 * based on a specific province.
 */
private[creation] object ProvinceCreation {

  private var created: Boolean = false

  /**
   * Create all the entities of the domain
   * for the entire province.
   */
  def create(province: Province): Unit = {
    if (!created) {
      ProvincePlacesCreation.create(province)
      ProvinceLinesCreation.create(province)
      created = true
    }
  }

}
