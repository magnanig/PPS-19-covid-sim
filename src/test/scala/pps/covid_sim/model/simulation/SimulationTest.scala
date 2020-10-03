package pps.covid_sim.model.simulation

class SimulationTest {

  /*
  val cityAostaSimulation: CitySimulation = CitySimulation(Cities.AOSTA)
  val cityCesenaSimulation: CitySimulation = CitySimulation(Cities.AOSTA)
  val cityCerviaSimulation: CitySimulation = CitySimulation(Cities.AOSTA)

  val provinceAostaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.AOSTA, Seq(cityAostaSimulation))
  val provinceForliSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.FORLI_CESENA, Seq(cityCesenaSimulation))
  val provinceRavennaSimulation: ProvinceSimulation = ProvinceSimulation(Provinces.RAVENNA, Seq(cityCerviaSimulation))

  val regionEmiliaRomagnaSimulation: RegionSimulation = RegionSimulation(Regions.EMILIA_ROMAGNA,
    Seq(provinceRavennaSimulation, provinceForliSimulation))
  val regionValleAostaSimulation: RegionSimulation = RegionSimulation(Regions.VALLE_DAOSTA, Seq(provinceAostaSimulation))

  val italySimulation: NationSimulation = NationSimulation(Seq(regionValleAostaSimulation, regionEmiliaRomagnaSimulation))

  val infectedAosta = 3
  val infectedCesena = 10
  val infectedCervia = 5

  val aostaPeople: Seq[Person] = (1 to 20).map(_ => TestPerson(Cities.AOSTA, isInfected = false))
    .union((1 to infectedAosta).map(_ => TestPerson(Cities.AOSTA, isInfected = true)))

  val ravennaPeople: Seq[Person] = (1 to 20).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedCervia).map(_ => TestPerson(Cities.RAVENNA, isInfected = true)))

  val cesenaPeople: Seq[Person] = (1 to 50).map(_ => TestPerson(Cities.CESENA, isInfected = false))
    .union((1 to infectedCesena).map(_ => TestPerson(Cities.CESENA, isInfected = true)))

  val time: Calendar = ScalaCalendar(2020, 1, 1)

  @Before
  def init(): Unit = {
    cityAostaSimulation.updateInfectedCount(time, aostaPeople.count(_.isInfected))
    cityCesenaSimulation.updateInfectedCount(time, cesenaPeople.count(_.isInfected))
    cityCerviaSimulation.updateInfectedCount(time, ravennaPeople.count(_.isInfected))
  }

  @Test
  def testProvinceSimulation(): Unit = {
    assertEquals(infectedAosta, provinceAostaSimulation.infected(time))
    assertEquals(infectedCesena, provinceForliSimulation.infected(time))
    assertEquals(infectedCervia, provinceRavennaSimulation.infected(time))
  }

  @Test
  def testAggregations(): Unit = {
    assertEquals(infectedAosta, provinceAostaSimulation.infected(time))
    assertEquals(infectedCesena, provinceForliSimulation.infected(time))
    assertEquals(infectedCervia, provinceRavennaSimulation.infected(time))

    assertEquals(infectedAosta, regionValleAostaSimulation.infected(time))
    assertEquals(infectedCesena + infectedCervia, regionEmiliaRomagnaSimulation.infected(time))
    assertEquals(infectedAosta + infectedCesena + infectedCervia, italySimulation.infected(time))
  }
   */

}
