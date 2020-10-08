package pps.covid_sim.model.places.rooms

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.ClosedPlace
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.{LimitedPeopleLocation, Location}
import pps.covid_sim.model.places.arranging.Arrangeable
import pps.covid_sim.model.places.arranging.Placement.Placeholder
import pps.covid_sim.model.places.arranging.Tables.TablesGroup

abstract class MultiRoom[R <: Room](override val city: City,
                                    private var rooms: Seq[R] = Seq())
  extends LimitedPeopleLocation with ClosedPlace {

  override lazy val capacity: Int = rooms.map(_.capacity).sum

  def addRooms(rooms: Seq[R]): Unit = {
    this.rooms = this.rooms ++ rooms
  }

  def addRoom(room: R): Unit = {
    this.rooms :+ room
  }

  def getRooms: Seq[R] = rooms

  override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    rooms.foreach(_.propagateVirus(time, place)(covidInfectionParameters))
  }

  override def clear(): Unit = {
    super.clear()
    rooms.foreach(_.clear())
  }

  override protected def preEnter(group: Group, time: Calendar): Option[Room] = group match {
    //case Single(_: Employed) => Some(this)
    case _ if canEnter(group, time) => freeRoom(group, time) match {
      case Some((room: TablesRoom, Seq(tableGroup: TablesGroup))) => room.enter(group, time, tableGroup)
      case Some((room: Room, _)) => room.enter(group, time); Some(room)
      case _ => /*println(s"WARNING: No free room found for $group. Current rooms capacity: ${rooms
        .map(_.toString).mkString(";\n")}")*/
        None
    }
    case _ => None
  }

  override protected def preExit(group: Group): Unit = {
    rooms.find(_.currentGroups.contains(group)) match {
      case Some(room) => room.exit(group)
      case _ =>
    }
  }

  protected[places] def freeRoom(group: Group, time: Calendar): Option[(Room, Seq[Placeholder[Group]])] = rooms
    .collect {
      case arrangeable: Arrangeable[Group,_] => arrangeable.findAccommodation(group)
      case room if room.canEnter(group, time) => Some((room, Seq.empty))
    }.collect { case Some((room, places)) => (room, places) }
    .reduceOption(Ordering.by((_: (Room, Seq[Placeholder[Group]]))._2.map(_.capacity).sum).min)
}
