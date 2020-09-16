package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.transports.PrivateTransports.Car
import pps.covid_sim.model.transports.PublicTransports.{Bus, Carriage, Train}
import pps.covid_sim.util.time.Time.ScalaCalendar

class TransportsTest {

  val bus: Bus = Bus(3)
  val train: Train = Train(2)
  val car: Car = Car(2)

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

  val commuters: List[Single] = (1 to 40).map(i => Single(TestPerson(i, false))).toList

  val marco: Single = Single(TestPerson(41, false))
  val lorenzo: Single = Single(TestPerson(42, true))
  val gianmarco: Single = Single(TestPerson(43, true))
  val nicolas: Single = Single(TestPerson(44, false))

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)

  @Test
  def testBusTurnout(): Unit = {
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(nicolas, time))
    assertEquals(None, bus.enter(lorenzo, time))
  }

  @Test
  def testBusGroupTurnout(): Unit = {
    assertEquals(Option(bus), bus.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, bus.enter(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(4, false))), time))
  }

  @Test
  def testExitFromBusWithNoOneInside(): Unit = {
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(nicolas, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(None, bus.enter(gianmarco, time))
  }

  @Test
  def testDuplicateEntriesInBus(): Unit = {
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    assertEquals(None, bus.enter(nicolas, time))
  }

  @Test
  def testDuplicateGroupEntriesInBus(): Unit = {
    assertEquals(Option(bus), bus.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(Option(bus), bus.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, bus.enter(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(1, false))), time))
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    assertEquals(None, bus.enter(nicolas, time))
  }

  @Test
  def testCarTurnout(): Unit = {
    assertEquals(Option(car), car.enter(marco, time))
    assertEquals(Option(car), car.enter(lorenzo, time))
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(nicolas, time))
    assertEquals(None, car.enter(lorenzo, time))
  }

  @Test
  def testCarGroupTurnout(): Unit = {
    assertEquals(Option(car), car.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, car.enter(marco, time))
  }

  @Test
  def testExitFromCarWithNoOneInside(): Unit = {
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(marco, time))
    assertEquals(Option(car), car.enter(nicolas, time))
    assertEquals(None, car.enter(gianmarco, time))
  }

  @Test
  def testDuplicateEntriesInCar(): Unit = {
    assertEquals(Option(car), car.enter(lorenzo, time))
    assertEquals(Option(car), car.enter(lorenzo, time))
    assertEquals(Option(car), car.enter(gianmarco, time))
    assertEquals(None, car.enter(nicolas, time))
  }

  @Test
  def testDuplicateGroupEntriesInCar(): Unit = {
    assertEquals(Option(car), car.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(Option(car), car.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(None, car.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time))
  }

  @Test
  def testTrainTurnout(): Unit = {
    enterPeopleFromList(0, commuters.size - 2, commuters, train)
    assertEquals(Option(Carriage(20)), train.enter(marco, time))
    assertEquals(Option(Carriage(20)), train.enter(lorenzo, time))
    train.exit(lorenzo)
    assertEquals(Option(Carriage(20)), train.enter(nicolas, time))
    assertEquals(None, train.enter(lorenzo, time))
  }

  @Test
  def testTrainGroupTurnout(): Unit = {
    enterPeopleFromList(0, commuters.size - 4, commuters, train)
    assertEquals(Option(Carriage(20)), train.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time))
    assertEquals(Option(Carriage(20)), train.enter(Multiple(TestPerson(3, false), Set(TestPerson(3, false), TestPerson(4, false))), time))
    assertEquals(None, train.enter(marco, time))
    train.exit(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))))
    assertEquals(Option(Carriage(20)), train.enter(marco, time))
    assertEquals(Option(Carriage(20)), train.enter(nicolas, time))
    assertEquals(None, train.enter(lorenzo, time))
  }

  @Test
  def testExitFromTrainWithNoOneInside(): Unit = {
    train.exit(lorenzo)
    enterPeopleFromList(0, commuters.size-2, commuters,train)
    assertEquals(Option(Carriage(20)), train.enter(marco, time))
    assertEquals(Option(Carriage(20)), train.enter(lorenzo, time))
    train.exit(marco)
    assertEquals(Option(Carriage(20)), train.enter(marco, time))
  }

  @Test
  def testDuplicateEntriesInTrain(): Unit = {
    assertEquals(Option(Carriage(20)), train.enter(lorenzo, time))
    assertEquals(Option(train), train.enter(lorenzo, time))
    assertEquals(Option(Carriage(20)), train.enter(gianmarco, time))
    assertEquals(Option(train), train.enter(gianmarco, time))
  }

  @Test
  def testDuplicateGroupEntriesInTrain(): Unit = {
    val c = train.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time)
    val t =  train.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(2, false))), time)
    val n = train.enter(Multiple(TestPerson(1, false), Set(TestPerson(1, false), TestPerson(3, false))), time)
    println("CIAO")
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: List[Group], means: Transport ): Unit = {
    listPeople.slice(from, until) foreach(p => means.enter(p, time))
  }

}
