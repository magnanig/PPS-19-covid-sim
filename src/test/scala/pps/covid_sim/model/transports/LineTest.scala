package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.samples.{Cities, CovidParameters}
import pps.covid_sim.model.transports.PublicTransports._
import pps.covid_sim.util.RandomGeneration.randomBirthDate
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar


class LineTest {

  val cityTest: City = Cities.FORLI
  val place: Place = new Place {
    override val city: City = cityTest
    override val openedInLockdown: Boolean = false
    override def mask: Option[Masks.Mask] = None
  }

  // Number of buses per line, capacity of each bus (about 20 seats), time interval in which the line is available
  val busLine: BusLine = BusLine(2, 2, HoursInterval(8, 20))
  busLine.setCoveredCities(Set(cityTest))

  // Number of trains, number of carriages
  val trainLine: TrainLine = TrainLine(1, 2, Locality.Region.EMILIA_ROMAGNA, HoursInterval(15, 16))
  trainLine.setCoveredCities(Set(cityTest))

  var people: Seq[Person] = (0 to 40).map(_ => Worker(randomBirthDate(18, 70), cityTest))

  val commuters: Seq[Single] = (1 to 40).map(s => if (s % 2 == 0) Single(people(s))
  else Single(people(s))).toList

  val groupCommuters: Seq[Group] = (1 to 39 by 2).map(s => Multiple(people(s),
    Set(people(s), people(s + 1)))).toList

  println(groupCommuters)

  val marco: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val lorenzo: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val gianmarco: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val nicolas: Single = Single(Student(randomBirthDate(6, 24), cityTest))

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15) // only the last argument is relevant: '15' is the specific hour

  @Test
  def testBusLineUsage(): Unit = {
    assertTrue(busLine.isReachable(place.city))
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
    assertEquals(Some(Bus(2)), busLine.tryUse(Multiple(people(1),
      Set(people(1), people(2))), time))
    // TestPerson(2, false) is already using the line: the group does not enter
    assertEquals(None, busLine.tryUse(Multiple(people(3),
      Set(people(3), people(2))), time))
    assertEquals(None, busLine.tryUse(Multiple(people(1),
      Set(people(1), people(3))), time))
    assertEquals(None, busLine.tryUse(Multiple(people(1),
      Set(people(1), people(2))), time))
    assertEquals(Some(Bus(2)), busLine.tryUse(Multiple(people(3),
      Set(people(3), people(4))), time))
    // The line is full
    assertEquals(None, busLine.tryUse(Multiple(people(5),
      Set(people(5), people(6))), time))
    // Trying to get out a group that did not get on board together
    busLine.busList.head.exit(Multiple(people(1), Set(people(1), people(3))))
    assertEquals(2, busLine.busList.head.numCurrentPeople)
    assertEquals(4, busLine.busList.map(b => b.numCurrentPeople).sum)
    // Trying to get out a person that did get on board in group
    busLine.busList(1).exit(people(2))
    assertEquals(2, busLine.busList.head.numCurrentPeople)
    assertEquals(4, busLine.busList.map(b => b.numCurrentPeople).sum)
    // Exit of a group from the first bus
    busLine.busList.head.exit(Multiple(people(1), Set(people(1), people(2))))
    assertEquals(0, busLine.busList.head.numCurrentPeople)
    assertEquals(2, busLine.busList.map(b => b.numCurrentPeople).sum)
  }

  @Test
  def testTrainLineUsage(): Unit = {
    assertTrue(trainLine.isReachable(place.city))
    enterPeopleFromList(0, commuters.size, commuters, trainLine)
    // The line is full
    assertEquals((None, None), trainLine.tryUse(lorenzo, time))
  }

  @Test
  def testTrainLineGroupUsage(): Unit = {
    val (train, carriage) = trainLine.tryUse(Multiple(marco.leader, Set(marco.leader, lorenzo.leader)), time)
    assertEquals(train, Some(Train(2)))
    assertEquals(carriage, Some(Carriage(20)))
    // Filling the train
    enterPeopleFromList(0, groupCommuters.size, groupCommuters, trainLine)
    assertEquals((None, None), trainLine.tryUse(Multiple(people(1),
      Set(people(1), people(3))), time))
    assertEquals((None, None), trainLine.tryUse(Multiple(people(5),
      Set(people(5), people(6))), time))
    // Exit of a group from the first carriage
    train.get.exit(Multiple(people(3), Set(people(3), people(4))))
    assertEquals(18, carriage.get.numCurrentPeople)
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a group that is not in this carriage
    carriage.get.exit(Multiple(people(5), Set(people(5), people(6))))
    assertEquals(18, carriage.get.numCurrentPeople)
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a group that did not get on board together
    train.get.exit(Multiple(people(1), Set(people(1), people(8))))
    assertEquals(38, train.get.numCurrentPeople)
    // Trying to get out a person that did get on board in group
    train.get.exit(Single(lorenzo.leader))
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
    bus.get.propagateVirus(time, place)(CovidParameters)
  }

  @Test
  def testInfectionsInTrain(): Unit = {
    val (train, _) = trainLine.tryUse(marco, time)
    assertEquals(1, train.get.numCurrentPeople)
    enterPeopleFromList(0, commuters.size - 1, commuters, trainLine)
    // The train is full
    assertEquals((None,None), trainLine.tryUse(lorenzo, time))
    assertEquals(40, train.get.numCurrentPeople)
    println("People who have been infected:")
    train.get.propagateVirus(time, place)(CovidParameters)
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: Seq[Group], line: Line[PublicTransport]): Unit = {
    listPeople.slice(from, until).foreach(p =>
      line.tryUse(p, time))
  }

}
