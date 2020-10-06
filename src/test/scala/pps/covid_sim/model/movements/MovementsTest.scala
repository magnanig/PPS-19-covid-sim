package pps.covid_sim.model.movements

import java.util.Calendar

import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.movements.MovementFunctions.{linearPathWithWallFollowing, randomPath}
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.OpenPlaces.{Beach, Park}
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.Shops.SuperMarket
import pps.covid_sim.model.places.rooms.{DiscoRoom, GymRoom}
import pps.covid_sim.model.samples.Cities
import pps.covid_sim.util.geometry.{Coordinates, Speed}
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.MonthsInterval
import pps.covid_sim.util.time.Time.{Month, ScalaCalendar}


class MovementsTest {

  val cityTest: City = Cities.FORLI
  val timeTable: TimeTable = TimeTable(MonthsInterval(Month.JANUARY, Month.DECEMBER))
  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)

  val discoRoom: DiscoRoom = DiscoRoom(50)
  val supermarket: SuperMarket = SuperMarket(cityTest, 50, timeTable)
  val gymRoom: GymRoom = GymRoom(15)
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
    println(discoRoom.dimension)

    val pathsSampling = randomPath(discoRoom.dimension, discoRoom.obstacles, Speed.FAST, 1)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }

  @Test
  def testRandomMovementFunctionInBeach(): Unit = {
    (0 until 5).foreach(i => beach.enter(groupsOfTwo(i), time))
    (0 until 5).foreach(i => beach.enter(groupsOfThree(i), time))
    beach.currentGroups.flatten.foreach(_.position = Coordinates.randomOnBorder(beach.dimension))
    println(beach.dimension)

    val pathsSampling = randomPath(beach.dimension, beach.obstacles, Speed.FAST, 6)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }

  @Test
  def testRandomMovementFunctionInPark(): Unit = {
    (0 until 5).foreach(i => park.enter(groupsOfTwo(i), time))
    (0 until 5).foreach(i => park.enter(groupsOfThree(i), time))
    park.currentGroups.flatten.foreach(_.position = Coordinates.randomOnBorder(park.dimension))
    println(park.dimension)

    val pathsSampling = randomPath(park.dimension, park.obstacles, Speed.MIDDLE, 3)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }

  @Test
  def testLinearMovementWithPathFollowingFunctionInSupermarket(): Unit = {
    (0 until 5).foreach(i => supermarket.enter(groupsOfTwo(i), time))
    (0 until 5).foreach(i => supermarket.enter(groupsOfThree(i), time))
    supermarket.currentGroups.flatten.foreach(_.position = Coordinates.randomOnBorder(supermarket.dimension))
    println(supermarket.dimension)

    val pathsSampling = linearPathWithWallFollowing(supermarket.dimension, supermarket.obstacles, Speed.SLOW, 3)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }

  @Test
  def testLinearMovementWithPathFollowingFunctionInGym(): Unit = {
    (0 until 5).foreach(i => gymRoom.enter(groupsOfTwo(i), time))
    (0 until 5).foreach(i => gymRoom.enter(groupsOfThree(i), time))
    gymRoom.currentGroups.flatten.foreach(_.position = Coordinates.randomOnBorder(gymRoom.dimension))
    println(gymRoom.dimension)

    val pathsSampling = linearPathWithWallFollowing(gymRoom.dimension, gymRoom.obstacles, Speed.SLOW, 2)
    val paths = pathsSampling((groupsOfTwo.slice(0, 5) ++ groupsOfThree.slice(0, 5)).toSet)
    println(paths)
  }

}
