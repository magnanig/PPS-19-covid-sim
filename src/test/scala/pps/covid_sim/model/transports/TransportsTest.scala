package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.transports.PrivateTransports.Car
import pps.covid_sim.model.transports.PublicTransports.{Bus, Carriage, Train}
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

class TransportsTest {

  val bus: Bus = Bus(3, new HoursInterval(15, 16))
  val train: Train = Train(2, new HoursInterval(15, 16))
  val car: Car = Car(2)

  val marco: Single = Single(new Person {})
  val lorenzo: Single = Single(new Person {})
  val gianmarco: Single = Single(new Person {})
  val nicolas: Single = Single(new Person {})

  val commuters: List[Single] = (1 to 40).map(e=> Single(new Person {})).toList

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
    assertEquals(None, bus.enter(lorenzo, time))
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
  def testTrainTurnout(): Unit = {
    enterPeopleFromList(0, commuters.size-2, commuters,train)
    assertEquals(Option(Carriage(20)), train.enter(marco, time))
    assertEquals(Option(Carriage(20)), train.enter(lorenzo, time))
    train.exit(lorenzo)
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
    assertEquals(None, train.enter(lorenzo, time))
    assertEquals(Option(Carriage(20)), train.enter(gianmarco, time))
    assertEquals(None, train.enter(gianmarco, time))
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: List[Single], means: Transport ): Unit = {
    listPeople.slice(from,until) foreach(p => means.enter(p, time))
  }

}
