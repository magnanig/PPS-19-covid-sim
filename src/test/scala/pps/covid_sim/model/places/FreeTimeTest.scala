package pps.covid_sim.model.places

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.people.People.{Student, Unemployed, Worker}
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.FreeTime.Restaurant
import pps.covid_sim.model.places.arranging.Tables.TablesArrangement
import pps.covid_sim.model.places.rooms.TablesRoom
import pps.covid_sim.model.samples.Cities
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class FreeTimeTest {

  val timeTableRestaurant: TimeTable = TimeTable().add(Day.TUESDAY -> Day.SUNDAY, 12 -> 14, 19 -> 22)

  // crea una stanza con 1 fila di tavoli, la quale è composta da solo un tavolo avente 2 posti
  val room1: TablesRoom = TablesRoom(2, 2, TablesArrangement(numRows = 1, numPlacesInTablesGroup = 2))
  // crea una stanza con 1 fila di tavoli, la quale è composta da 3 tavoli:
  // a sinistra da 3 posti, al centro da 1 e a destra ancora da 4
  val room2: TablesRoom = TablesRoom(3, 3, TablesArrangement(numRows = 1, numPlacesInTablesGroup = 3, 1, 1))

  val alCaminettoR: Restaurant = Restaurant(Cities.CERVIA, timeTableRestaurant, openedInLockdown = false, List(room1, room2))
  val ilMoroR: Restaurant = Restaurant(Cities.CERVIA, timeTableRestaurant, openedInLockdown = false, List(room1))
  val laGreppiaR: Restaurant = Restaurant(Cities.CESENA, timeTableRestaurant, openedInLockdown = false, List(room2))
  val peccatoDiGolaR: Restaurant = Restaurant(Cities.CESENATICO, timeTableRestaurant, openedInLockdown = false, List(room1))

  val tizio: Person = Worker(ScalaCalendar(1956, 10, 29), Cities.CESENATICO)
  val caio: Person = Worker(ScalaCalendar(1962, 9, 21), Cities.CESENATICO)
  val pippo: Person = Worker(ScalaCalendar(1989, 11, 29), Cities.CESENATICO)
  val paperino: Person = Worker(ScalaCalendar(1992, 7, 24), Cities.CESENATICO)
  val gianmarco: Person = Student(ScalaCalendar(1997, 1, 26), Cities.CERVIA)
  val vincenzo: Person = Unemployed(ScalaCalendar(1997, 7, 30), Cities.CESENA)
  val multiGroup_1: Group = Multiple(gianmarco, Set(gianmarco, vincenzo))
  val multiGroup_2: Group = Multiple(pippo, Set(pippo, paperino))


  @Test
  def testEnterToRestaurant(): Unit = {
    val openTime = ScalaCalendar(2020, 8, 4, 13)
    val closeTime = ScalaCalendar(2020, 8, 4, 16)

    assertEquals(0, peccatoDiGolaR.numCurrentPeople)
    assert(peccatoDiGolaR.enter(multiGroup_1, closeTime).isEmpty)
    assert(peccatoDiGolaR.enter(paperino, closeTime).isEmpty)
    assertEquals(0, peccatoDiGolaR.numCurrentPeople)

    assertEquals(0, ilMoroR.numCurrentPeople)
    assert(ilMoroR.enter(pippo, openTime).isDefined)  // assigning a table for 2 to one person
    assertEquals(1, ilMoroR.numCurrentPeople)
    assert(ilMoroR.enter(multiGroup_1, openTime).isEmpty) //reject group request from 2 people
    assertEquals(1, ilMoroR.numCurrentPeople)
    ilMoroR.exit(pippo)
    assertEquals(0, ilMoroR.numCurrentPeople)
    assert(ilMoroR.enter(multiGroup_1, openTime).isDefined)
    assertEquals(2, ilMoroR.numCurrentPeople)
    ilMoroR.exit(multiGroup_1)
    assertEquals(0, ilMoroR.numCurrentPeople) // empty ilMoroRestaurant

    assertEquals(0, alCaminettoR.numCurrentPeople) // available tables: 3,1,1,2
    assert(alCaminettoR.enter(tizio, openTime).isDefined) // assigning a single table one person
    assertEquals(1, alCaminettoR.numCurrentPeople) // available tables: 3,1,2
    assert(alCaminettoR.enter(multiGroup_1, openTime).isDefined) // assigning a table for 2 to 2 person
    assertEquals(3, alCaminettoR.numCurrentPeople) // available tables: 3,1
    assert(alCaminettoR.enter(multiGroup_2, openTime).isDefined) // assign a table for 3 to 2 person
    assertEquals(5, alCaminettoR.numCurrentPeople) // available tables: 1
    assert(alCaminettoR.enter(pippo, openTime).isDefined) // assigning a table for 1 to one person
    assertEquals(6, alCaminettoR.numCurrentPeople) // available tables: -- (None)
    assert(alCaminettoR.enter(paperino, openTime).isEmpty) // sorry, we are full
    assertEquals(6, alCaminettoR.numCurrentPeople) // tables all occupied
    assert(alCaminettoR.enter(caio, openTime).isEmpty)
    alCaminettoR.exit(tizio)
    alCaminettoR.exit(pippo)
    alCaminettoR.exit(paperino)
    alCaminettoR.exit(multiGroup_1)
    alCaminettoR.exit(multiGroup_2)
    assertEquals(0, alCaminettoR.numCurrentPeople)
  }

}