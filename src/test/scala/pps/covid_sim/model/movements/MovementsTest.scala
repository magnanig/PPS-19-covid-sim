package pps.covid_sim.model.movements

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.movements.MovementFunctions.randomPath
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.OpenPlaces.{Beach, Park}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.places.Shops.SuperMarket
import pps.covid_sim.model.places.rooms.{DiscoRoom, GymRoom}
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.{MonthsInterval, Time}
import pps.covid_sim.util.time.Time.{Month, ScalaCalendar}

class MovementsTest {

  val cityTest: City = City(1, "Forlì", 118000, Province(1, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val timeTable: TimeTable = TimeTable(MonthsInterval(Month.JANUARY, Month.DECEMBER))
  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)

  val discoRoom: DiscoRoom = DiscoRoom(50)
  val supermarket: SuperMarket = SuperMarket(cityTest, 50, timeTable)
  //val gymRoom: GymRoom = GymRoom(15)
  val beach: Beach = Beach(cityTest)
  val park: Park = Park(cityTest)

  // Dummy Person implementations, used for testing purposes only
  case class TestPerson(idCode: Int, infected: Boolean) extends Person  {

    override val residence: City = null

    override val birthDate: Calendar = null

    override lazy val age: Int = 0

    override def infectionPlaceInstance: Option[Place] = ???

    override def infectionPlace: Option[Class[_ <: Place]] = ???

    override def friends: Set[Person] = Set()

    val id: Int = idCode

    override def wornMask: Option[Masks.Mask] = ???

    override def canInfect: Boolean = infected

    override def isInfected: Boolean = false

    override def isRecovered: Boolean = false

    override def isDeath: Boolean = false

    override def infects(place: Place, time: Calendar): Unit = ???

    override def infectedPeopleMet: Set[Person] = ???

    override def metInfectedPerson(person: Person): Unit = ???
  }

  var people: Seq[Person] = (0 to 150).map(i => TestPerson(i, false))

  var groupsOfTwo: Seq[Group] = (0 to 50 by 2).map(s => Multiple(people(s), Set(people(s), people(s + 1)))).toList
  //println(groupsOfTwo)
  var groupsOfThree: Seq[Group] = (52 to 130 by 3).map(s => Multiple(people(s),
    Set(people(s), people(s + 1), people(s + 2)))).toList
  //println(groupsOfThree)
  var groupsOfSix: Seq[Group] = (133 to 145 by 6).map(s => Multiple(people(s),
    Set(people(s), people(s + 1), people(s + 2), people(s + 3), people(s + 4), people(s + 5)))).toList
  //println(groupsOfSix)

  @Test
  def testRandomMovementFunctionInDiscoRoom(): Unit = {
    (0 until 5).foreach(i => discoRoom.enter(groupsOfTwo(i), time))
    (0 until 5).foreach(i => discoRoom.enter(groupsOfThree(i), time))
    discoRoom.currentGroups.flatten.foreach(_.position = Coordinates.randomOnBorder(discoRoom.dimension))
    println(discoRoom.currentGroups.flatten.map(person => person.position).toList)
    println(discoRoom.dimension)

    val pathsSampling = randomPath(discoRoom.dimension, discoRoom.obstacles, Speed.FAST, 1)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }


}
