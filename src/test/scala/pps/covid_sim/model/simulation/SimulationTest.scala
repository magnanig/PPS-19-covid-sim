package pps.covid_sim.model.simulation

import java.util.Calendar

import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.samples.{Cities, Provinces, Regions, TestPerson}
import pps.covid_sim.model.simulation.Aggregation.{NationSimulation, RegionSimulation}
import pps.covid_sim.util.time.Time.ScalaCalendar

class SimulationTest {

  val provinceAostaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.FORLI_CESENA)
  val provinceRavennaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.RAVENNA)
  val provinceCesenaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.FORLI_CESENA)

  val regionEmiliaRomagnaSimulation: RegionSimulation = RegionSimulation(Regions.EMILIA_ROMAGNA,
    Seq(provinceRavennaSimulation, provinceCesenaSimulation))
  val regionValleAostaSimulation: RegionSimulation = RegionSimulation(Regions.VALLE_DAOSTA, Seq(provinceAostaSimulation))

  val italySimulation: NationSimulation = NationSimulation(Seq(regionValleAostaSimulation, regionEmiliaRomagnaSimulation))

  val infectedAosta = 3
  val infectedCesena = 10
  val infectedRavenna = 5

  val aostaPeople: Seq[Person] = (1 to 20).map(_ => TestPerson(Cities.AOSTA, isInfected = false))
    .union((1 to infectedAosta).map(_ => TestPerson(Cities.AOSTA, isInfected = true)))

  val ravennaPeople: Seq[Person] = (1 to 20).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedRavenna).map(_ => TestPerson(Cities.RAVENNA, isInfected = true)))

  val cesenaPeople: Seq[Person] = (1 to 50).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedCesena).map(_ => TestPerson(Cities.CESENA, isInfected = true)))

  val time: Calendar = ScalaCalendar(2020, 1, 1)

  @Before
  def init(): Unit = {
    provinceAostaSimulation.updateInfectedCount(time, aostaPeople.count(_.isInfected))
    provinceRavennaSimulation.updateInfectedCount(time, ravennaPeople.count(_.isInfected))
    provinceCesenaSimulation.updateInfectedCount(time, cesenaPeople.count(_.isInfected))
  }

  @Test
  def testProvinceSimulation(): Unit = {
    assertEquals(infectedAosta, provinceAostaSimulation.infected(time))
    assertEquals(infectedRavenna, provinceRavennaSimulation.infected(time))
    assertEquals(infectedCesena, provinceCesenaSimulation.infected(time))
  }

  @Test
  def testAggregations(): Unit = {
    val time = ScalaCalendar(2020, 1, 1)
    assertEquals(infectedAosta, regionValleAostaSimulation.infected(time))
    assertEquals(infectedCesena + infectedRavenna, regionEmiliaRomagnaSimulation.infected(time))
    assertEquals(infectedAosta + infectedCesena + infectedRavenna, italySimulation.infected(time))
  }

}
