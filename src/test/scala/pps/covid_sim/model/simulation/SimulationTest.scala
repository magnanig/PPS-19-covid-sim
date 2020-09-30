package pps.covid_sim.model.simulation

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.samples.{Cities, Provinces, Regions, TestPerson}
import pps.covid_sim.model.simulation.Aggregation.RegionSimulation
import pps.covid_sim.util.time.Time.ScalaCalendar

class SimulationTest {

  val provinceRavennaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.RAVENNA)
  val provinceCesenaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.FORLI_CESENA)
  val regionEmiliaRomagnaSimulation: RegionSimulation = RegionSimulation(Regions.EMILIA_ROMAGNA,
    Seq(provinceRavennaSimulation, provinceCesenaSimulation))

  val infectedCesena = 10
  val infectedRavenna = 5

  val cesenaPeople: Seq[Person] = (1 to 50).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedCesena).map(_ => TestPerson(Cities.CESENA, isInfected = true)))

  val ravennaPeople: Seq[Person] = (1 to 20).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedRavenna).map(_ => TestPerson(Cities.RAVENNA, isInfected = true)))

  @Test
  def testRegionSimulation(): Unit = {
    val time = ScalaCalendar(2020, 1, 1)
    provinceCesenaSimulation.updateInfectedCount(time, cesenaPeople.count(_.isInfected))
    provinceRavennaSimulation.updateInfectedCount(time, ravennaPeople.count(_.isInfected))
    assertEquals(infectedCesena + infectedRavenna, regionEmiliaRomagnaSimulation.infected(time))
  }

  // TODO finish
}
