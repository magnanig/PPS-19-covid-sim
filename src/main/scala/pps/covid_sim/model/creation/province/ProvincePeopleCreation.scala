package pps.covid_sim.model.creation.province

import pps.covid_sim.model.creation.PeopleCreation
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province}

private[province] object ProvincePeopleCreation {

  /**
   * Creates all people within a specific province.
   *
   * @param province  province where people will be created.
   * @return          list of all people who have been created.
   */
  def create(province: Province): List[Person] = {
    new ProvincePeopleCreation(province).create()
  }

}

private class ProvincePeopleCreation(val province: Province) extends PeopleCreation {

  override val _cities: Set[City] = ProvinceCitiesCreation.create(province)

}
