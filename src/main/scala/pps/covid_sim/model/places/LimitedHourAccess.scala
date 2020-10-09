package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.scheduling.TimeTable
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.Day.Day

trait LimitedHourAccess extends Location {

  /**
   * The location's time tables, indicating the moments when it is open and accessible.
   */
  val timeTable: TimeTable

  override def isOpen(day: Day): Boolean = timeTable.isDefinedOn(day)

  override def isOpen(datesInterval: DatesInterval): Boolean = timeTable.isDefinedBetween(datesInterval)

  override protected[places] def canEnter(group: Group, time: Calendar): Boolean = timeTable.isDefinedAt(time)


}