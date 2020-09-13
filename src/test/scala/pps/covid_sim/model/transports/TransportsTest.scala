package pps.covid_sim.model.transports

import java.util.logging.LogRecord

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.transports.PrivateTransports.Car
import pps.covid_sim.model.transports.PublicTransports.{Bus, Carriage, Train}
import pps.covid_sim.util.time.HoursInterval

class TransportsTest {

  val bus: Bus = Bus(3, new HoursInterval(15, 16))
  val train: Train = Train(2, new HoursInterval(15, 16))
  val car: Car = Car(2)

  val marco: Single = Single(new Person {})
  val lorenzo: Single = Single(new Person {})
  val gianmarco: Single = Single(new Person {})
  val nicolas: Single = Single(new Person {})

  val commuters: List[Single] = (1 to 40).map(e=> Single(new Person {})).toList

  @Test
  def testBusTurnout(): Unit = {
    assertEquals(Option(bus), bus.enter(marco))
    assertEquals(Option(bus), bus.enter(lorenzo))
    assertEquals(Option(bus), bus.enter(gianmarco))
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(nicolas))
    assertEquals(None, bus.enter(lorenzo))
  }

  @Test
  def testExitFromBusWithNoOneInside(): Unit = {
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(marco))
    assertEquals(Option(bus), bus.enter(nicolas))
    assertEquals(Option(bus), bus.enter(lorenzo))
    assertEquals(None, bus.enter(gianmarco))
  }

  @Test
  def testDuplicateEntriesInBus(): Unit = {
    assertEquals(Option(bus), bus.enter(marco))
    assertEquals(Option(bus), bus.enter(lorenzo))
    assertEquals(None, bus.enter(lorenzo))
    assertEquals(Option(bus), bus.enter(gianmarco))
    assertEquals(None, bus.enter(nicolas))
  }

  @Test
  def testCarTurnout(): Unit = {
    assertEquals(Option(car), car.enter(marco))
    assertEquals(Option(car), car.enter(lorenzo))
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(nicolas))
    assertEquals(None, car.enter(lorenzo))
  }

  @Test
  def testExitFromCarWithNoOneInside(): Unit = {
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(marco))
    assertEquals(Option(car), car.enter(nicolas))
    assertEquals(None, car.enter(gianmarco))
  }

  @Test
  def testDuplicateEntriesInCar(): Unit = {
    assertEquals(Option(car), car.enter(lorenzo))
    assertEquals(None, car.enter(lorenzo))
    assertEquals(Option(car), car.enter(gianmarco))
    assertEquals(None, car.enter(nicolas))
  }

  @Test
  def testTrainTurnout(): Unit = {
    enterPeopleFromList(0,commuters.size-2,commuters,train)
    assertEquals(Option(Carriage(20)), train.enter(marco))
    assertEquals(Option(Carriage(20)), train.enter(lorenzo))
    train.exit(lorenzo)
    assertEquals(Option(Carriage(20)), train.enter(nicolas))
    assertEquals(None, train.enter(lorenzo))
  }

  @Test
  def testExitFromTrainWithNoOneInside(): Unit = {
    train.exit(lorenzo)
    enterPeopleFromList(0,commuters.size-2,commuters,train)
    assertEquals(Option(Carriage(20)), train.enter(marco))
    assertEquals(Option(Carriage(20)), train.enter(lorenzo))
    train.exit(marco)
    assertEquals(Option(Carriage(20)), train.enter(marco))
  }

  @Test
  def testDuplicateEntriesInTrain(): Unit = {
    assertEquals(Option(Carriage(20)), train.enter(lorenzo))
    assertEquals(None, train.enter(lorenzo))
    assertEquals(Option(Carriage(20)), train.enter(gianmarco))
    assertEquals(None, train.enter(gianmarco))
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: List[Single], means: Transport ): Unit = {
    listPeople.slice(from,until) foreach(p => means.enter(p))
  }

}
