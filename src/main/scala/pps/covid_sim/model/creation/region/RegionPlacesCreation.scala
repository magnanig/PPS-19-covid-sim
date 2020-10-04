package pps.covid_sim.model.creation.region

import pps.covid_sim.model.creation.PlacesCreation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.Region

private[region] object RegionPlacesCreation {

  /**
   * Create all places throughout the region. This computation also involves
   * the creation of all people and all cities of the region. In addition to
   * creating the different places, it also takes care of assigning students
   * and professors to their respective schools and/or universities.
   * Finally, it also assigns all workers to their respective jobs.
   *
   * @note          All the places that will be created, at the end of
   *                the computation, will be contained within the
   *                PlacesContainer object.
   *                The same goes for the cities and people that will be
   *                contained in CitiesContainer and PeopleContainer
   *                respectively.
   * @param region  region in which all places will be created.
   */
  def create(region: Region): Unit = {
    new RegionPlacesCreation(region).create()
  }

}

private class RegionPlacesCreation(val region: Region) extends PlacesCreation {

  override val _people: List[Person] = RegionPeopleCreation.create(region)

}