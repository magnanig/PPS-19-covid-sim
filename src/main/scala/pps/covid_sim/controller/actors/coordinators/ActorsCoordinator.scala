package pps.covid_sim.controller.actors.coordinators

import java.util.Calendar

import akka.actor.{Props, ReceiveTimeout}
import pps.covid_sim.controller.actors.CoordinatorCommunication.{Init, SetProvince, SetRegion}
import pps.covid_sim.controller.actors.coordinators.ActorsCoordination._
import pps.covid_sim.model.container.CitiesContainer
import pps.covid_sim.model.people.actors.Communication.{Acknowledge, HourTick, Lockdown, Stop}
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.immutable.ParSet

/** *
 * The main Coordinator of level 0. It manage a subSet of RegionCoordinator
 */
case class ActorsCoordinator() extends Coordinator {

  private var localMaxInfections: Int = 0
  private var nextAvailableLockdown: Calendar = _
  private var lockdown: Boolean = false

  override def receive: Receive = {
    case Init(area, di) =>
      area match {
        case province: Province => this.createProvinceActors(province) //Create directly only the province coordinator
        case region: Region => this.createActors(Set(region)) //Create only one region coordinator
        case Locality.Italy() => this.createActors(CitiesContainer.getRegions) //Create all the regions
      }
      simulationInterval = di
      currentTime = di.from
      //currentTime = currentTime + 1
      this.nextAvailableLockdown = di.from
      println("Started")
      tick()
    case Acknowledge() if waitingAck.contains(sender) => waitingAck -= sender
      if (waitingAck.isEmpty) nextStep()
    case ReceiveTimeout => println("WARNING: Timeout!"); nextStep()
    case Stop => endSimulation()
    case msg => println(s"[TOP] Not expected: $msg")
  }

  private def nextStep(): Unit = {
    waitingAck = _subordinatedActors
    if (currentTime == simulationInterval.until) endSimulation() else tick()
  }

  private def endSimulation(): Unit = {
    controller.notifyRunEnded()
    _subordinatedActors.foreach(s => s ! Stop())
    context.stop(self)
  }

  private def tick(): Unit = {
    if (currentTime.hour == 0) {
      currentInfections = Statistic(controller.people).numCurrentPositive
      println(s"Infection on ${currentTime.getTime}: $currentInfections")
      if (currentInfections > localMaxInfections) localMaxInfections = currentInfections
    }
    checkLockdown(currentTime)
    controller.tick(currentTime)
    println()
    println("----->Tick<-----")
    currentTime = currentTime + 1
    _subordinatedActors.foreach(_ ! HourTick(currentTime))

  }

  private[controller] def stopSimulation(): Unit = synchronized {
    actorsCoordinator ! Stop()
    system.terminate()
  }

  private def checkLockdown(time: Calendar): Unit = {
    if (!lockdown && time >= nextAvailableLockdown && currentInfections > controller.covidInfectionParameters.lockDownStart * controller.people.size) {
      println("Start lockdown")
      controller.startLockdown(time, currentInfections)
      lockdown = true
      _subordinatedActors.foreach(_ ! Lockdown(lockdown))
    } else if (lockdown && currentInfections < controller.covidInfectionParameters.lockDownEnd * localMaxInfections) {
      println("End lockdown")
      nextAvailableLockdown = time ++ 30
      controller.endLockdown(time, currentInfections)
      localMaxInfections = 0
      lockdown = false
      _subordinatedActors.foreach(_ ! Lockdown(lockdown))
    }
  }

  private def createActors(regions: Set[Region]): Unit = {
    val regionActors = regions.map(r => {
      println(s"TOPC: Total regions considered in the simulation: ${regions.size} " + "coordinatore principale crea: " + r.name)
      val actor = system.actorOf(Props[RegionCoordinator])
      actor ! SetRegion(r)
      actor -> r
    }).toMap
    _subordinatedActors = regionActors.keySet.par
    this.waitingAck = _subordinatedActors
  }

  def createProvinceActors(province: Province): Unit = {
    val provinceActor = system.actorOf(Props[ProvinceCoordinator])
    this._subordinatedActors = ParSet(provinceActor)
    this.waitingAck = _subordinatedActors
    provinceActor ! SetProvince(province)
  }
}
