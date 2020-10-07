package pps.covid_sim.model.creation.work

import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.Shops.{ClothesShop, Shop, SuperMarket}
import pps.covid_sim.model.samples.Places
import pps.covid_sim.util.RandomGeneration.randomIntInRange
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.scheduling.Planning.WorkPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._

import scala.util.Random

private[work] case class ShopCreation() {

  def create(city: City,
             workerPerShop: List[Int],
             workers: List[Worker],
             capacityRange: (Int, Int),
             staffRange: (Int, Int),
             random: Random = new Random()): List[Place] = {

    def createShop(numWorkers: Int)(shopBuilder: () => Shop): List[Shop] = {
      var shops: List[Shop] = List()
      val totalWorker: Int = workers.size
      var numWorker: Int = 0

      while (numWorker < numWorkers) {
        val shop: Shop = shopBuilder()
        // number of workers (people) who will be assigned to the shop
        val bound: Int = Statistic.getMin(numWorker + randomIntInRange(staffRange._1, staffRange._2, random), totalWorker)
        workers.slice(numWorker, bound).foreach(worker => { // add WorkPlan to each worker
          val plan: WorkPlan[Shop] = WorkPlan()
            .add(shop, Day.TUESDAY -> Day.SUNDAY, 9 -> 13, 15 -> 19)
            .commit()
          shop.addWorkPlan(worker, plan)
          worker.setWorkPlace(shop)
        })
        numWorker = bound
        shops = shop :: shops
      }
      shops
    }

    createShop(workerPerShop.head)(() => SuperMarket(city, randomIntInRange(capacityRange._1, capacityRange._2, random),
      Places.SHOP_TIME_TABLE)) :::
      createShop(workerPerShop.last)(() => ClothesShop(city, randomIntInRange(capacityRange._1, capacityRange._2, random),
        Places.SHOP_TIME_TABLE))
  }
}
