package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.transports.PublicTransports.{Bus, BusLine, TrainLine}
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar


class LineTest {

  val city: City = City(1, "Forlì", 50, Province(3, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val place: Place = new Place {
    override val city: City = city
  }

  val busLine: BusLine = BusLine(2, 2, HoursInterval(15, 16))
  busLine.setCoveredCities(Set(city))

  val trainLine: TrainLine = TrainLine(2, 2, HoursInterval(15, 16))
  trainLine.setCoveredCities(Set(city))

  val marco: Single = Single(new Person {})
  val lorenzo: Single = Single(new Person {})
  val gianmarco: Single = Single(new Person {})
  val nicolas: Single = Single(new Person {})

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)

  @Test
  def testBusLineUsage(): Unit = {
    assertEquals(true, busLine.isReachable(place))
    /*assertTrue(lineBus.tryUse(lorenzo,time).isInstanceOf[Bus])
    assertTrue(lineBus.tryUse(marco,time).isInstanceOf[Bus])
    assertTrue(lineBus.tryUse(gianmarco,time).isInstanceOf[Bus])*/


    /*assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(nicolas, time))
    assertEquals(None, bus.enter(lorenzo, time))*/
  }

  @Test
  def testTrainLineUsage(): Unit = {
    
  }



}
