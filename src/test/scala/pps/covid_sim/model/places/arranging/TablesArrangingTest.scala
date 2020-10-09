package pps.covid_sim.model.places.arranging

import java.util.Calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.places.FreeTime.Bar
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.arranging.Tables.TablesArrangement
import pps.covid_sim.model.places.rooms.{Room, TablesRoom}
import pps.covid_sim.model.samples.Cities
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class TablesArrangingTest {

  val tablesRoom: TablesRoom = TablesRoom(4, 2, new TablesArrangement(1, 2, 1, 1))
  val bar: Bar = Bar(Cities.CERVIA,
    TimeTable()
      .add(Day.MONDAY -> Day.SUNDAY, 8 -> 13, 16 -> 21),
    openedInLockdown = false,
    Seq(tablesRoom)
  )
  val birthDate: Calendar = ScalaCalendar(1997, 1, 1)
  val mario: Student = Student(birthDate, Cities.CERVIA)
  val luigi: Student = Student(birthDate, Cities.CERVIA)
  val fabio: Student = Student(birthDate, Cities.CERVIA)
  val luca: Student = Student(birthDate, Cities.CERVIA)
  val group: Group = Multiple(luca, Set(luca, fabio))

  @Test
  def testCorrectAssignment(): Unit = {

    val openTime = ScalaCalendar(2020, 1, 1, 12)
    assert(bar.enter(mario, openTime).isDefined)
    var assignedRoom: TablesRoom = bar.getRooms.find(_.getTableGroup(mario).isDefined).get
    assertEquals(1, bar.numCurrentPeople)
    assertEquals(1, assignedRoom.getTableGroup(mario).get.capacity)

    assert(bar.enter(luigi, openTime).isDefined)
    assignedRoom = bar.getRooms.find(_.getTableGroup(luigi).isDefined).get
    assertEquals(2, bar.numCurrentPeople)
    assertEquals(1, assignedRoom.getTableGroup(luigi).get.capacity)

    assert(bar.enter(fabio, openTime).isDefined)
    assignedRoom = bar.getRooms.find(_.getTableGroup(fabio).isDefined).get
    assertEquals(3, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(fabio).get.capacity)

    assert(bar.enter(luca, openTime).isEmpty) // people capacity would be 4 but tables are only 3...

    bar.exit(fabio)
    assert(!bar.getRooms.exists(_.getTableGroup(fabio).isDefined))

    assert(bar.enter(group, openTime).isDefined)
    assignedRoom = bar.getRooms.find(_.getTableGroup(group).isDefined).get
    assertEquals(4, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(group).get.capacity)

    bar.exit(group)
    assertEquals(2, bar.numCurrentPeople)

    assert(bar.enter(luca, openTime).isDefined)
    assignedRoom = bar.getRooms.find(_.getTableGroup(luca).isDefined).get
    assertEquals(3, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(luca).get.capacity)

  }

}

object TablesArrangingTest {
  implicit def locationToRoom[R <: Room](location: Location): R = location.asInstanceOf[R]
}
