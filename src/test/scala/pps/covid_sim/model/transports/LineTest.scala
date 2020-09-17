package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.transports.PublicTransports.{Bus, BusLine, Carriage, Line, Train, TrainLine}
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar


class LineTest {

  val cityTest: City = City(1, "Forlì", 50, Province(3, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val place: Place = new Place {
    override val city: City = cityTest
  }

  val busLine: BusLine = BusLine(2, 2, HoursInterval(8, 20))
  busLine.setCoveredCities(Set(cityTest))

  val trainLine: TrainLine = TrainLine(1, 2, Locality.Region.EMILIA_ROMAGNA, HoursInterval(15, 16))
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

  val commuters: List[Single] = (1 to 40).map(s => if (s % 2 == 0) Single(TestPerson(s, false))
                                                   else Single(TestPerson(s, true))).toList

  val groupCommuters: List[Group] = (1 to 39 by 2).map(s => Multiple(TestPerson(s, false),
                                                       Set(TestPerson(s, false), TestPerson(s + 1, false)))).toList

  val marco: Single = Single(TestPerson(41, false))
  val lorenzo: Single = Single(TestPerson(42, true))
  val gianmarco: Single = Single(TestPerson(43, true))
  val nicolas: Single = Single(TestPerson(44, false))

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15) // only the last argument is relevant: '15' is the specific hour

  @Test
  def testBusLineUsage(): Unit = {
    assertTrue(busLine.isReachable(place))
    assertEquals(Some(Bus(2)), busLine.tryUse(lorenzo, time))
    // Lorenzo is already in: this entry is ignored
    assertEquals(None, busLine.tryUse(lorenzo, time))
    assertEquals(Some(Bus(2)), busLine.tryUse(marco, time))
    val means = busLine.tryUse(nicolas,time)
    assertTrue(means.get.isInstanceOf[Bus])
    means.get.exit(lorenzo)
    assertEquals(Some(Bus(2)), busLine.tryUse(gianmarco, time))
    assertEquals(None, busLine.tryUse(gianmarco, time)) // Gianmarco is already using the line
    assertEquals(Some(Bus(2)), busLine.tryUse(lorenzo, time))
  }

  @Test
  def testBusLineGroupUsage(): Unit = {
    assertEquals(Some(Bus(2)), busLine.tryUse(Multiple(TestPerson(1, false),
                                              Set(TestPerson(1, false), TestPerson(2, false))), time))
    // TestPerson(2, false) is already using the line: the group does not enter
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(3, false),
                                      Set(TestPerson(3, false), TestPerson(2, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false),
                                      Set(TestPerson(1, false), TestPerson(3, false))), time))
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(1, false),
                                      Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(Some(Bus(2)), busLine.tryUse(Multiple(TestPerson(3, false),
                                      Set(TestPerson(3, false), TestPerson(4, false))), time))
    // The line is full
    assertEquals(None, busLine.tryUse(Multiple(TestPerson(5, false),
                                      Set(TestPerson(5, false), TestPerson(6, false))), time))
    // Trying to get out a group that did not get on board together
    busLine.busList(0).exit(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))))
    assertEquals(2, busLine.busList(0).numCurrentPeople)
    assertEquals(4, busLine.busList.map(b => b.numCurrentPeople).sum)
    // Trying to get out a person that did get on board in group
    busLine.busList(1).exit(Single(TestPerson(3, false)))
    assertEquals(2, busLine.busList(0).numCurrentPeople)
    assertEquals(4, busLine.busList.map(b => b.numCurrentPeople).sum)
    // Exit of a group from the first bus
    busLine.busList(0).exit(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))))
    assertEquals(0, busLine.busList(0).numCurrentPeople)
    assertEquals(2, busLine.busList.map(b => b.numCurrentPeople).sum)
  }

  @Test
  def testTrainLineUsage(): Unit = {
    assertTrue(trainLine.isReachable(place))
    enterPeopleFromList(0, commuters.size, commuters, trainLine)
    // The line is full
    assertEquals((None, None), trainLine.tryUse(lorenzo, time))
  }

  @Test
  def testTrainLineGroupUsage(): Unit = {
    val (train, carriage) = trainLine.tryUse(Multiple(marco.leader, Set(marco.leader, lorenzo.leader)), time)
    assertEquals(train, Some(Train(2)))
    assertEquals(carriage, Some(Carriage(20)))
    // Filling the first carriage
    enterPeopleFromList(0, 9, groupCommuters, trainLine)
    // Filling the second carriage (now the train is full)
    enterPeopleFromList(9, groupCommuters.size, groupCommuters, trainLine)
    assertEquals((None, None), trainLine.tryUse(Multiple(TestPerson(1, false),
                                                Set(TestPerson(1, false), TestPerson(3, false))), time))
    assertEquals((None, None), trainLine.tryUse(Multiple(TestPerson(5, false),
                                                Set(TestPerson(5, false), TestPerson(6, false))), time))
    // Exit of a group from the first carriage
    train.get.exit(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(4, false))))
    assertEquals(18, carriage.get.numCurrentPeople)
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a group that is not in this carriage
    carriage.get.exit(Multiple(TestPerson(5, false), Set(TestPerson(5, false), TestPerson(6, false))))
    assertEquals(18, carriage.get.numCurrentPeople)
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a group that did not get on board together
    train.get.exit(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(8, false))))
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a person that did get on board in group
    train.get.exit(Single(TestPerson(1, false)))
    assertEquals(38, train.get.numCurrentPeople)
  }

  @Test
  def testInfectionsInBus(): Unit = {
    val busLine: BusLine = BusLine(1, 10, HoursInterval(8, 20))
    busLine.setCoveredCities(Set(cityTest))
    val bus = busLine.tryUse(marco, time)
    assertEquals(1, bus.get.numCurrentPeople)
    enterPeopleFromList(0, 9, commuters, busLine)
    // The bus is full
    assertEquals(None, bus.get.enter(lorenzo, time))
    assertEquals(10, bus.get.numCurrentPeople)
    println("People who have been infected:")
    bus.get.propagateVirus(time, place)
  }

  @Test
  def testInfectionsInTrain(): Unit = {
    val (train, carriage) = trainLine.tryUse(marco, time)
    assertEquals(1, train.get.numCurrentPeople)
    enterPeopleFromList(0, commuters.size - 1, commuters, trainLine)
    // The train is full
    assertEquals((None,None), trainLine.tryUse(lorenzo, time))
    assertEquals(40, train.get.numCurrentPeople)
    println("People who have been infected:")
    train.get.propagateVirus(time, place)
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: List[Group], line: Line): Unit = {
    listPeople.slice(from, until).foreach(p => line.tryUse(p, time))
  }

}
