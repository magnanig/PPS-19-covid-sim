package pps.covid_sim.model.transports

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.transports.PrivateTransports.Car
import pps.covid_sim.model.transports.PublicTransports.{Bus, Train}
import pps.covid_sim.util.time.HoursInterval

class TransportsTest {

  val bus: Bus = Bus(3, new HoursInterval(15, 16))
  val train: Train = Train(2, new HoursInterval(15, 16))
  val car: Car = Car(4)

  val marco: Single = Single(new Person {})
  val lorenzo: Single = Single(new Person {})
  val gianmarco: Single = Single(new Person {})
  val nicolas: Single = Single(new Person {})

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

}
