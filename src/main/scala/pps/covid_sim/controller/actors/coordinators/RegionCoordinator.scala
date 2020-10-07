package pps.covid_sim.controller.actors.coordinators

import java.util.Calendar

import akka.actor.{Props, ReceiveTimeout}
import pps.covid_sim.controller.actors.CoordinatorCommunication.{SetProvince, SetRegion}
import pps.covid_sim.controller.actors.coordinators.ActorsCoordination.{actorsCoordinator, system}
import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.actors.Communication.{Acknowledge, HourTick, Stop}
import pps.covid_sim.model.places.Locality.{Province, Region}

/** *
 * A Coordinator of the level 1. It manage a subSet of ProvinceCoordinator
 */
case class RegionCoordinator() extends Coordinator {

  implicit protected var _region: Region = _ // will be initialized later when the SetRegion message will be received
  private var _myProvinces: Set[Province] = _ // Will be initialized later when the SetProvince message will be received

  override def receive: Receive = {
    case SetRegion(region) => this._region = region
      this._myProvinces = CitiesContainer.getProvince(region)
      this.createActors(this._myProvinces)
    case HourTick(currentTime) => this.spreadTick(_region, currentTime)
    case Acknowledge() if this.waitingAck.contains(sender) => this.waitingAck -= sender
      if (this.waitingAck.isEmpty) sendAck()
    case ReceiveTimeout => sendAck(); println("WARNING: Timeout! Sono un sotto coordinatore: Region:" + _region)
    case Stop() => this.endSimulation()
    case msg => println(s"Not expected [Region]: $msg")
  }

  private def createActors(provinces: Set[Province]): Unit = {
    val provinceActors = provinces.map(province => {
      println(s"REGC: Total people considered in the simulation: ${provinces.size} " + "of province: " + province)
      val actor = system.actorOf(Props[ProvinceCoordinator])
      actor ! SetProvince(province)
      actor -> province
    }).toMap

    this._subordinatedActors = provinceActors.keySet.par
    this.waitingAck = _subordinatedActors
  }

  private def sendAck(): Unit = {
    this.waitingAck = _subordinatedActors
    actorsCoordinator ! Acknowledge()
  }

  private def spreadTick(region: Region, currentTime: Calendar): Unit = { //esempio test
    println(region)
    this.waitingAck = _subordinatedActors
    //context.setReceiveTimeout(Duration.create(50, TimeUnit.MILLISECONDS))
    this._subordinatedActors.foreach(s => s ! HourTick(currentTime))
  }

  private def endSimulation(): Unit = {
    _subordinatedActors.foreach(s => s ! Stop())
    context.stop(self)
  }
}
