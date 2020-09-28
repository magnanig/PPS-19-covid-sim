package pps.covid_sim.model.creation.work

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.parameters.CreationParameters.{clothesShopPercentage, superMarketPercentage}
import pps.covid_sim.util.Statistic

import scala.util.Random

case class WorkPlacesCreation() {

  def create(city: City, workerPerWorkPlace: List[Int], workers: List[Worker],
             random: Random = new Random()): List[Place] = {

    val workerPerShop: List[Int] = Statistic.totalPercentageToInt(workerPerWorkPlace.last,
      superMarketPercentage, clothesShopPercentage)
    var workPlaces: List[Place] = List()
    var index: Int = 0

    workPlaces = workPlaces ::: CompanyCreation().create(city,
      workers.slice(index, index + workerPerWorkPlace.head),
      officesRange = if (city.isProvince) (10, 300) else (3, 20),
      capacityRange = if (city.isProvince) (10, 60) else (2, 20), random)
    index += workerPerWorkPlace.head

    workPlaces = workPlaces ::: FactoryCreation().create(city,
      workers.slice(index, index + workerPerWorkPlace(1)),
      officesRange = if (city.isProvince) (10, 300) else (3, 20),
      capacityRange = if (city.isProvince) (20, 200) else (8, 20), random)
    index += workerPerWorkPlace(1)

    workPlaces = workPlaces ::: ShopCreation().create(city,
      workerPerShop,
      workers.slice(index, index + workerPerWorkPlace.last),
      capacityRange = if (city.isProvince) (50, 600) else (15, 80),
      staffRange = if (city.isProvince) (40, 80) else (2, 8), random)

    workPlaces

  }

}
