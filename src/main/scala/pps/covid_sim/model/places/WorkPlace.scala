package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.people.People.Employed
import pps.covid_sim.model.people.PeopleGroup.{Group, Single}
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.scheduling.Planning.WorkPlan

trait WorkPlace[T <: Location] extends Place {

  private var _workPlans: Map[Employed, WorkPlan[T]] = Map()

  def addWorkPlan(employed: Employed, workPlan: WorkPlan[T]): WorkPlace[T] = {
    _workPlans = _workPlans + (employed -> workPlan)
    this
  }

  def workPlans: Map[Employed, WorkPlan[T]] = _workPlans

  def getWorkPlan(employed: Employed): Option[WorkPlan[T]] = _workPlans.get(employed)

  override protected[places] def canEnter(group: Group, time: Calendar): Boolean = group match {
    case Single(employed: Employed) => _workPlans.get(employed) match {
      case Some(workPlan) => workPlan.isDefinedBetween(time)
      case _ => super.canEnter(group, time)
    }
    case _ => super.canEnter(group, time)
  }

}
