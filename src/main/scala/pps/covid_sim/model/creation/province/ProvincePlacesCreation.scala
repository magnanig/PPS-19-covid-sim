package pps.covid_sim.model.creation.province

import pps.covid_sim.model.creation.PlacesCreation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Province

private[province] object ProvincePlacesCreation {

  /**
   * Create all places throughout the province. This computation also involves
   * the creation of all people and all cities of the province. In addition to
   * creating the different places, it also takes care of assigning students
   * and professors to their respective schools and/or universities.
   * Finally, it also assigns all workers to their respective jobs.
   *
   * @note            All the places that will be created, at the end of
   *                  the computation, will be contained within the
   *                  PlacesContainer object.
   *                  The same goes for the cities and people that will be
   *                  contained in CitiesContainer and PeopleContainer
   *                  respectively.
   * @param province  province in which all places will be created.
   */
  def create(province: Province): Unit = {
    new ProvincePlacesCreation(province: Province).create()
  }

}

private class ProvincePlacesCreation(val province: Province) extends PlacesCreation {

  override val _people: List[Person] = ProvincePeopleCreation.create(province)

}
