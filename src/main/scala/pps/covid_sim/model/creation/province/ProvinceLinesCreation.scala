package pps.covid_sim.model.creation.province

import pps.covid_sim.model.container.{CitiesContainer, TransportLinesContainer}
import pps.covid_sim.model.places.Locality.Province
import pps.covid_sim.model.transports.PublicTransports.BusLine
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.HoursInterval

import scala.util.Random

private[province] object ProvinceLinesCreation {

  /**
   * Creates all transport line within a specific province.
   *
   * @param province  province where transport line will be created.
   */
  def create(province: Province): Unit = {
    new ProvinceLinesCreation(province).create()
  }

}

private class ProvinceLinesCreation(val province: Province) {

  private val random: Random = new Random()

  def create(): Unit = {
    val busLine: BusLine = BusLine(randomBusesPerLine(), randomPlacesPerBus(), HoursInterval(6, 20))
    busLine.setCoveredCities(CitiesContainer.getCities(province))
    TransportLinesContainer.add(busLine)
  }

  private def randomBusesPerLine(): Int = {
    RandomGeneration.randomIntInRange(40, 400, random)
  }

  private def randomPlacesPerBus(): Int = {
    RandomGeneration.randomIntInRange(20, 80, random)
  }

}
