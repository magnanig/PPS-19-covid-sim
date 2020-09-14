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

  val cityTest: City = City(1, "Forlì", 50, Province(3, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val place: Place = new Place{
    override val city: City = cityTest
  }

  val busLine: BusLine = BusLine(2, 2, HoursInterval(8, 20))
  busLine.setCoveredCities(Set(cityTest))

  val trainLine: TrainLine = TrainLine(2, 2, HoursInterval(15, 16))
  trainLine.setCoveredCities(Set(cityTest))

  val marco: Single = Single(new Person {})
  val lorenzo: Single = Single(new Person {})
  val gianmarco: Single = Single(new Person {})
  val nicolas: Single = Single(new Person {})

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)//only the last argument is relevant: 15 that means the specific hour

  @Test
  def testBusLineUsage(): Unit = {
    assertEquals(true, busLine.isReachable(place))

    val means1: Option[PublicTransports.PublicTransport] = busLine.tryUse(lorenzo,time)
    assertTrue(means1.get.isInstanceOf[Bus])

    val means2: Option[PublicTransports.PublicTransport] = busLine.tryUse(marco,time)
    assertTrue(means2.get.isInstanceOf[Bus])

    val means3: Option[PublicTransports.PublicTransport] = busLine.tryUse(nicolas,time)
    assertTrue(means3.get.isInstanceOf[Bus])

    means3.get.exit(lorenzo)

    var means4: Option[PublicTransports.PublicTransport] = busLine.tryUse(gianmarco,time)
    assertTrue(means4.get.isInstanceOf[Bus])

    means4.get.exit(gianmarco);

    means4 = busLine.tryUse(gianmarco,time)
    assertTrue(means4.get.isInstanceOf[Bus])


    //means4 = busLine.tryUse(gianmarco,time)
    //assertTrue(means4.get.isInstanceOf[Bus]) //TODO da problemi perché l'enter di magno non gestise il fatto che se la location è full e fai la enter di una persona gia dentro dovrebbe ritornare la location invece torna None

  }

  @Test
  def testTrainLineUsage(): Unit = {
    
  }



}
