package pps.covid_sim.model.creation.region

import pps.covid_sim.model.creation.PeopleCreation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Region}

private[region] object RegionPeopleCreation {

  /**
   * Creates all people within a specific region.
   *
   * @param region  region where people will be created.
   * @return        list of all people who have been created.
   */
  def create(region: Region): List[Person] = {
    new RegionPeopleCreation(region).create()
  }

}

private class RegionPeopleCreation(val region: Region) extends PeopleCreation {

  override val _cities: Set[City] = RegionCitiesCreation.create(region)

}
