package pps.covid_sim.model.places.rooms

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.DelimitedSpace
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.arranging.Arrangeable
import pps.covid_sim.model.places.arranging.Tables.{TablesArrangement, TablesGroup}
import pps.covid_sim.util.geometry.{Dimension, Rectangle}

case class TablesRoom(private val minCapacity: Int,
                      maxTableGroupCapacity: Int,
                      override protected val arrangement: TablesArrangement) extends Room with Arrangeable[Group, TablesGroup] {

  def this(minCapacity: Int, maxTableGroupCapacity: Int) = this(minCapacity, maxTableGroupCapacity,
    TablesArrangement.randomFor(minCapacity, maxTableGroupCapacity))

  override val capacity: Int = arrangement.totalCapacity

  override val dimension: Dimension = DelimitedSpace.randomDimension(capacity, 1, 100)

  override val mask: Option[Mask] = None

  /**
   * Get the tables group assigned to the desired group, if any.
   * @param group   the requested group
   * @return        the optional tables group assigned to group
   */
  def getTableGroup(group: Group): Option[TablesGroup] = synchronized { arrangement.getItemGroup(group) }

  override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    super.propagateVirus(time, place)(covidInfectionParameters)
    lookForFriends(place, time)(covidInfectionParameters)
  }

  override def toString: String = s"Room($numCurrentPeople / $capacity)\n  Tables state:\n" +
    s"${arrangement
      .map(table => s"    Table(${table.capacity}) -> ${
        if (table.isFree) "free" else s"assigned to ${table.assignee.get}"}")
      .mkString("\n")
    }"

  protected override def preEnter(group: Group, time: Calendar): Option[Room] = findAccommodation(group) match {
    case Some((_, Seq(tableGroup))) => tableGroup.assign(group); Some(this)
    case _ => None
  }

  protected override def preExit(group: Group): Unit = {
    arrangement.getItemGroup(group) match {
      case Some(tables: TablesGroup) => tables.release()
      case _ =>
    }
  }

  override protected[places] def findAccommodation(group: Group): Option[(Room, Seq[TablesGroup])] = arrangement.rows
    .flatMap(_.itemGroups)
    .filter(table => table.isFree && table.capacity - group.size >= 0)
    .reduceOption(Ordering.by((_: TablesGroup).capacity).min) match {
    case Some(table) => Some(this, Seq(table))
    case _ => None
  }

  private[places] def enter(group: Group, time: Calendar, tableGroup: TablesGroup): Option[Room] = {
    if (canEnter(group, time) && tableGroup.assign(group)) {
      super.addGroup(group)
      Some(this)
    } else None
  }

  override val obstacles: Set[Rectangle] = placeObstacles(dimension)

  /**
   * In TablesRoom obstacles (i.e. tables) are not considered explicitly,
   * since people motion is not implemented (it is assumed that people remain mostly at their table).
   * @param dimension the dimension of current space
   * @return          an empty Set
   */
  private def placeObstacles(dimension: Dimension): Set[Rectangle] = Set.empty

}

object TablesRoom {
  def apply(minCapacity: Int, maxTableGroupCapacity: Int): TablesRoom = new TablesRoom(
    minCapacity, maxTableGroupCapacity, TablesArrangement.randomFor(minCapacity, maxTableGroupCapacity))
}
