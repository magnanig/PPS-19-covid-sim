package pps.covid_sim.model.creation.region

import pps.covid_sim.model.container.{CitiesContainer, TransportLinesContainer}
import pps.covid_sim.model.places.Locality.Region
import pps.covid_sim.model.transports.PublicTransports.{BusLine, TrainLine}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.HoursInterval

import scala.util.Random

private[region] object RegionLinesCreation {

  /**
   * Creates all transport line within a specific region.
   *
   * @param region  region where transport line will be created.
   */
  def create(region: Region): Unit = {
    new RegionLinesCreation(region).create()
  }

}

private class RegionLinesCreation(val region: Region) {

  private val random: Random = new Random()

  def create(): Unit = {
    CitiesContainer.getProvince(region).foreach(province => {
      val busLine: BusLine = BusLine(randomBusesPerLine(), randomPlacesPerBus(), HoursInterval(6, 20))
      busLine.setCoveredCities(CitiesContainer.getCities(province))
      TransportLinesContainer.add(busLine)
    })

    val trainLine: TrainLine = TrainLine(randomTrainPerRegion(), randomCarriagesPerTrain(), region, HoursInterval(5, 23))
    trainLine.setCoveredCities(CitiesContainer.getCities(region))
    TransportLinesContainer.add(trainLine)
  }

  private def randomBusesPerLine(): Int = {
    RandomGeneration.randomIntInRange(40, 400, random)
  }

  private def randomPlacesPerBus(): Int = {
    RandomGeneration.randomIntInRange(20, 80, random)
  }

  private def randomTrainPerRegion(): Int = {
    RandomGeneration.randomIntInRange(2, 8, random)
  }

  private def randomCarriagesPerTrain(): Int = {
    RandomGeneration.randomIntInRange(2, 12, random)
  }

}
