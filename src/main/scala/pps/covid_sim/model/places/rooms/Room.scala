package pps.covid_sim.model.places.rooms

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.DelimitedSpace
import pps.covid_sim.model.places.Locations.LimitedPeopleLocation

trait Room extends LimitedPeopleLocation with DelimitedSpace {
  override protected def preEnter(group: Group, time: Calendar): Option[Room] = super.preEnter(group, time)
    .map(_ => this)
}
