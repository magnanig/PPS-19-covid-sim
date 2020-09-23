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
import pps.covid_sim.model.places.samples.Cities
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class ArrangeableTest {

  val tablesRoom: TablesRoom = TablesRoom(4, 2, new TablesArrangement(1, 2, 1, 1))
  val bar: Bar = Bar(Cities.CERVIA,
    TimeTable()
      .add(Day.MONDAY -> Day.SUNDAY, 8 -> 13, 16 -> 21),
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
    import ArrangeableTest.locationToRoom

    val openTime = ScalaCalendar(2020, 1, 1, 12)
    var assignedRoom: TablesRoom = bar.enter(mario, openTime).get // note: optional must be defined at this time
    assertEquals(1, bar.numCurrentPeople)
    assertEquals(1, assignedRoom.getTableGroup(mario).get.capacity)

    assignedRoom = bar.enter(luigi, openTime).get
    assertEquals(2, bar.numCurrentPeople)
    assertEquals(1, assignedRoom.getTableGroup(luigi).get.capacity)

    assignedRoom = bar.enter(fabio, openTime).get
    assertEquals(3, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(fabio).get.capacity)

    assert(bar.enter(luca, openTime).isEmpty) // people capacity would be 4 but tables are only 3...

    bar.exit(fabio)

    assignedRoom = bar.enter(group, openTime).get
    assertEquals(4, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(group).get.capacity)

    bar.exit(group)
    assertEquals(2, bar.numCurrentPeople)

    assignedRoom = bar.enter(luca, openTime).get
    assertEquals(3, bar.numCurrentPeople)
    assertEquals(2, assignedRoom.getTableGroup(luca).get.capacity)

  }

}

object ArrangeableTest {
  implicit def locationToRoom[R <: Room](location: Location): R = location.asInstanceOf[R]
}