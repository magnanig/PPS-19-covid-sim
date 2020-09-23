package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.people.People.Employed
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locations.{LimitedPeopleLocation, Location}

trait ClosedWorkPlace[T <: Location] extends ClosedPlace with LimitedPeopleLocation with WorkPlace[T] {

  override protected[places] def canEnter(group: Group, time: Calendar): Boolean = group.leader match {
    case employed: Employed if workPlans.contains(employed) => true
    case _ => currentGroups
      .filter(g => !g.leader.isInstanceOf[Employed] || !workPlans.contains(g.leader.asInstanceOf[Employed]))
      .map(_.size)
      .sum < capacity
  }
}
