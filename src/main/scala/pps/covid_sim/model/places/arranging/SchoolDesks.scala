package pps.covid_sim.model.places.arranging

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.arranging.Placement.{ItemArrangement, ItemGroup, Placeholder, Row}
import pps.covid_sim.util.RandomGeneration

object SchoolDesks {

  case class DesksArrangement(numRows: Int, numDesksInGroup: Int*)
    extends ItemArrangement[DeskGroup](numRows, _ => ClassroomRow(numDesksInGroup.map(DeskGroup))) {

    val desks: Seq[Desk] = this.rows
      .flatMap(_.itemGroups)
      .flatMap(_.adjacentDesks)

    override val totalCapacity: Int = desks.size

    def getDeskAt(numRow: Int, numDeskGroup: Int, numDesk: Int): Desk = rows(numRow)
      .itemGroups(numDeskGroup)
      .adjacentDesks(numDesk)

    override def getItemGroup(group: Group): Option[DeskGroup] = this.rows
      .flatMap(_.itemGroups)
      .find(_.adjacentDesks.exists(_.assignee.contains(group)))

    override def iterator: Iterator[DeskGroup] = rows.flatMap(_.itemGroups).iterator
  }

  object DesksArrangement {
    def randomFor(numPerson: Int): DesksArrangement = {
      val columns = RandomGeneration.randomIntInRange(1, 6)
      val numPlacesInEachGroup = (1 to columns).map(_ => RandomGeneration.randomIntInRange(1, 10)) //TODO: da migliorare
      val rows = Math.ceil(numPerson.toDouble / numPlacesInEachGroup.sum).toInt
      DesksArrangement(rows, numPlacesInEachGroup:_*)
    }
  }


  case class ClassroomRow(override val itemGroups: Seq[DeskGroup]) extends Row[DeskGroup]


  case class DeskGroup(numDesks: Int) extends ItemGroup {
    val adjacentDesks: List[Desk] = (1 to numDesks).map(_ => Desk()).toList

    override val capacity: Int = adjacentDesks.size

    override def isFree: Boolean = adjacentDesks.forall(_.isFree)

    override def assign(group: Group): Boolean = if(adjacentDesks.count(_.isFree) >= group.size) {
      adjacentDesks
        .zip(group)
        .foreach(e => e._1.assign(e._2.asInstanceOf[Student]))
      true
    } else {
      false
    }

    override def release(): Unit = adjacentDesks.foreach(_.release())

    override def propagateVirus(place: Location, time: Calendar)(covidInfectionParameters: CovidInfectionParameters): Unit = {
      adjacentDesks.map(_.assignee).sliding(2)
        .collect({ case List(Some(student1), Some(student2)) => (student1, student2) })
        .foreach(e => VirusPropagation(covidInfectionParameters).tryInfect(e._1, e._2, place, time))
    }
  }

  case class Desk() extends Placeholder[Student] {
    private var _assignee: Option[Student] = None

    def assignee: Option[Student] = _assignee

    override def isFree: Boolean = _assignee.isEmpty

    override def assign(student: Student): Boolean = _assignee match {
      case None => _assignee = Some(student); true
      case _ => false
    }

    override def release(): Unit = { _assignee = None }

    override val capacity: Int = if (isFree) 1 else 0
  }

}
