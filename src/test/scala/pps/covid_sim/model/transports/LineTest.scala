package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.transports.PublicTransports.{Bus, BusLine, Line, TrainLine}
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar


class LineTest {

  val cityTest: City = City(1, "Forlì", 50, Province(3, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val place: Place = new Place {
    override val city: City = cityTest
  }

  val busLine: BusLine = BusLine(2, 2, HoursInterval(8, 20))
  busLine.setCoveredCities(Set(cityTest))

  val trainLine: TrainLine = TrainLine(2, 2, Locality.Region.EMILIA_ROMAGNA, HoursInterval(15, 16))
  trainLine.setCoveredCities(Set(cityTest))

  // Dummy Person implementations, used for testing purposes only
  case class TestPerson(idCode: Int, infected: Boolean) extends Person  {

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

  val commuters: List[Single] = (1 to 40).map(i => if (i % 2 == 0) Single(TestPerson(i, false)) else Single(TestPerson(i, true))).toList

  val marco: Single = Single(TestPerson(41, false))
  val lorenzo: Single = Single(TestPerson(42, true))
  val gianmarco: Single = Single(TestPerson(43, true))
  val nicolas: Single = Single(TestPerson(44, false))

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15) // only the last argument is relevant: 15 that means the specific hour

  @Test
  def testBusLineUsage(): Unit = {
    assertTrue(busLine.isReachable(place))

    val means1: Option[PublicTransports.PublicTransport] = busLine.tryUse(lorenzo,time)
    assertTrue(means1.get.isInstanceOf[Bus])

    val means2: Option[PublicTransports.PublicTransport] = busLine.tryUse(marco,time)
    assertTrue(means2.get.isInstanceOf[Bus])

    val means3: Option[PublicTransports.PublicTransport] = busLine.tryUse(nicolas,time)
    assertTrue(means3.get.isInstanceOf[Bus])

    means3.get.exit(lorenzo)

    var means4: Option[PublicTransports.PublicTransport] = busLine.tryUse(gianmarco,time)
    assertTrue(means4.get.isInstanceOf[Bus])

    // No more seats available on this line
    assertEquals(None, busLine.tryUse(gianmarco,time))
  }

  @Test
  def testBusLineGroupUsage(): Unit = {
    assertEquals(Some(Bus(2)), busLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(2, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time))
  }

  @Test
  def testTrainLineUsage(): Unit = {
    assertTrue(trainLine.isReachable(place))
    val train = trainLine.tryUse(marco, time)
    enterPeopleFromList(0, commuters.size - 1, commuters, trainLine)
    assertEquals(None, trainLine.tryUse(lorenzo, time)) // the train is full
  }

  @Test
  def testTrainLineGroupUsage(): Unit = {
    assertEquals(Some(Bus(2)), trainLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(2, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time))
  }

  @Test
  def testInfectionsInBus(): Unit = {
    val busLine: BusLine = BusLine(2, 10, HoursInterval(8, 20))
    busLine.setCoveredCities(Set(cityTest))

    val bus = busLine.tryUse(marco, time)
    println(bus.get.numCurrentPeople)

    enterPeopleFromList(0, 9, commuters, busLine)

    // The bus is full
    assertEquals(None, bus.get.enter(lorenzo, time))
    println(bus.get.numCurrentPeople)

    println("People who have been infected:")
    bus.get.propagateVirus(time, place)
  }

  @Test
  def testInfectionsInTrain(): Unit = {
    val train = trainLine.tryUse(marco, time)
    //println(train._2.get.numCurrentPeople)

    enterPeopleFromList(0, commuters.size - 1, commuters, trainLine)

    // The train is full
    assertEquals(None, trainLine.tryUse(lorenzo, time))
    //println(train._2.get.numCurrentPeople)

    println("People who have been infected:")
    train._2.get.propagateVirus(time, place)

  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: List[Group], line: Line): Unit = {
    listPeople.slice(from, until).foreach(p => line.tryUse(p, time))
  }



}
