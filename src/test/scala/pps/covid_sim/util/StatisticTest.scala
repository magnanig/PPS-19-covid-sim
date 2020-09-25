package pps.covid_sim.util

import java.util.Calendar

import org.junit.Test
import org.junit.Assert.assertEquals
import pps.covid_sim.util
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.model.people.{Person, PersonTmp}
import pps.covid_sim.model.creation.CitiesObject
import pps.covid_sim.model.places.Locality._
import pps.covid_sim.model.places.samples.Cities

class StatisticTest {

  @Test
  def testMiddleAgeCurrentPositive(): Unit = {
    var age: List[Int] = List()
    var people: List[Person] = List()

    // these people must participate in the calculation of the average age as they are currently positive
    (1 to 10000).foreach(_ => {
      val date: Calendar = RandomGeneration.randomBirthDate()
      people = PersonTmp(date, Cities.CERVIA, infected = true) :: people
      age = (Calendar.getInstance() -- date) :: age
    })

    // add another 100 non-positive people, who must not participate in the calculation of the average age
    (1 to 100).foreach(_ => {
      people = PersonTmp(RandomGeneration.randomBirthDate(), Cities.CERVIA) :: people
    })

    assertEquals(Math.round((age.sum.toDouble / 10000.0).toFloat), Statistic(people).middleAgeCurrentPositive())
  }

  @Test
  def testCurrentPositive(): Unit = {
    val date: Calendar = ScalaCalendar(1997, 1, 26)
    var people: List[Person] = List()

    CitiesObject.getCities(Region.FRIULI_VENEZIA_GIULIA)
      .foreach(city => people = PersonTmp(date, city, infected = true) :: people)
    CitiesObject.getCities(Region.TRENTINO_ALTO_ADIGE)
      .foreach(city => people = PersonTmp(date, city) :: people)

    val statistic: Statistic = util.Statistic(people)

    assertEquals(CitiesObject.getCities(Region.FRIULI_VENEZIA_GIULIA).size, statistic.numCurrentPositive())

    assertEquals(0, statistic.numCurrentPositive(CitiesObject.getProvince("RA")))

    assertEquals(CitiesObject.getCities("UD").size,
      statistic.numCurrentPositive(CitiesObject.getProvince("UD")))

    assertEquals(CitiesObject.getCities(Region.FRIULI_VENEZIA_GIULIA).size,
      statistic.numCurrentPositive(Region.FRIULI_VENEZIA_GIULIA))

    assertEquals(0, statistic.numCurrentPositive(Region.TRENTINO_ALTO_ADIGE))
  }

  @Test
  def testRecoveredPeople(): Unit = {
    val date: Calendar = ScalaCalendar(1997, 1, 26)
    var people: List[Person] = List()

    CitiesObject.getCities(Region.EMILIA_ROMAGNA)
      .foreach(city => people = PersonTmp(date, city, recovered = true) :: people)
    CitiesObject.getCities(Region.TRENTINO_ALTO_ADIGE)
      .foreach(city => people = PersonTmp(date, city) :: people)

    val statistic: Statistic = util.Statistic(people)

    assertEquals(CitiesObject.getCities(Region.EMILIA_ROMAGNA).size, statistic.numRecovered())

    assertEquals(0, statistic.numRecovered(CitiesObject.getProvince("FI")))

    assertEquals(CitiesObject.getCities("RN").size,
      statistic.numRecovered(CitiesObject.getProvince("RN")))

    assertEquals(CitiesObject.getCities(Region.EMILIA_ROMAGNA).size,
      statistic.numRecovered(Region.EMILIA_ROMAGNA))

    assertEquals(0, statistic.numRecovered(Region.TRENTINO_ALTO_ADIGE))
  }

  @Test
  def testDeathsPeople(): Unit = {
    val date: Calendar = ScalaCalendar(1997, 1, 26)
    var people: List[Person] = List()

    CitiesObject.getCities(Region.CAMPANIA).foreach(city => people = PersonTmp(date, city, death = true) :: people)
    CitiesObject.getCities(Region.TRENTINO_ALTO_ADIGE).foreach(city => people = PersonTmp(date, city) :: people)

    val statistic: Statistic = util.Statistic(people)

    assertEquals(CitiesObject.getCities(Region.CAMPANIA).size, statistic.numDeaths())

    assertEquals(0, statistic.numDeaths(CitiesObject.getProvince("TO")))

    assertEquals(CitiesObject.getCities("NA").size,
      statistic.numDeaths(CitiesObject.getProvince("NA")))

    assertEquals(CitiesObject.getCities(Region.CAMPANIA).size,
      statistic.numDeaths(Region.CAMPANIA))

    assertEquals(0, statistic.numDeaths(Region.TRENTINO_ALTO_ADIGE))

  }

  @Test
  def testConfirmedCases(): Unit = {

    val date: Calendar = ScalaCalendar(1997, 1, 26)
    var people: List[Person] = List()

    CitiesObject.getCities(Region.CALABRIA).foreach(city => people = PersonTmp(date, city, infected = true) :: people)
    CitiesObject.getCities(Region.VENETO).foreach(city => people = PersonTmp(date, city, recovered = true) :: people)
    CitiesObject.getCities(Region.MOLISE).foreach(city => people = PersonTmp(date, city, death = true) :: people)

    val statistic: Statistic = util.Statistic(people)

    assertEquals(CitiesObject.getCities(Region.CALABRIA).size + CitiesObject.getCities(Region.VENETO).size +
      CitiesObject.getCities(Region.MOLISE).size, statistic.numConfirmedCases())

    assertEquals(CitiesObject.getCities(Region.MOLISE).size, statistic.numConfirmedCases(Region.MOLISE))

    assertEquals(CitiesObject.getCities(Region.VENETO).size, statistic.numConfirmedCases(Region.VENETO))

    assertEquals(0, statistic.numConfirmedCases(Region.SARDEGNA))

    assertEquals(0, statistic.numConfirmedCases(CitiesObject.getProvince("BO")))

    assertEquals(CitiesObject.getCities("CB").size,
      statistic.numConfirmedCases(CitiesObject.getProvince("CB")))

  }

}
