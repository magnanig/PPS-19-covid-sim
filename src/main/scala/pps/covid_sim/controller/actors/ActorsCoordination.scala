package pps.covid_sim.controller.actors

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props, ReceiveTimeout}
import pps.covid_sim.controller.ControllerImpl
import pps.covid_sim.controller.actors.ActorsCoordination.currentTime
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.util.time.DatesInterval

import scala.collection.parallel.ParSet
import scala.util.Random
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.Statistic
import pps.covid_sim.parameters.{CovidInfectionParameters, CreationParameters}
import pps.covid_sim.people.actors.Communication.{Acknowledge, Lockdown, SetProvince, SetRegion, Tick}

import scala.concurrent.duration.Duration

object ActorsCoordination {

  private[actors] case class Init(controller: ControllerImpl, datesInterval: DatesInterval)
  private[actors] case class Stop()

  private[actors] var system: ActorSystem = _
  private[actors] var actorsCoordinator: ActorRef = _

  private var controller: ControllerImpl = _
  private var simulationInterval: DatesInterval = _
  private var simulation: Simulation = _
  private var currentTime: Calendar = _


  private[controller] def create(controller: ControllerImpl, datesInterval: DatesInterval): Unit = {
    system = ActorSystem.create()
    actorsCoordinator = system.actorOf(Props[ActorsCoordinator])
    actorsCoordinator ! Init(controller, datesInterval)
  }
  /***
   * The main Coordinator of level 0. It manage a subSet of RegionCoordinator
   */
  class ActorsCoordinator extends Actor with Coordinator {

    private var localMaxInfections: Int = 0
    private var nextAvailableLockdown: Calendar = _
    private var lockdown: Boolean = false

    //private var regionCoordinators: ParSet[ActorRef] = _

    override def receive: Receive = {
      case Init(c, di) => createActors(c.regions)
        controller = c
        simulationInterval = di
        simulation = Simulation(c.people)
        currentTime = di.from
        this.waitingAck = _subordinatedActors
        this.nextAvailableLockdown = di.from
        println("Started")
        tick()
      case Acknowledge() if waitingAck.contains(sender) => waitingAck -= sender
        if (waitingAck.isEmpty) nextStep()
      case ReceiveTimeout => println("WARNING: Timeout!"); nextStep()
      case Stop => endSimulation()
      case msg => println(s"Not expected: $msg")
    }

    private def nextStep(): Unit = {
      waitingAck = _subordinatedActors
      if (currentTime == simulationInterval.until) endSimulation() else tick()
    }

    private def endSimulation(): Unit = {
      simulation.close()
      controller.simulationEnded(simulation)
      _subordinatedActors.foreach(context.stop)//TODO stoppare prima le persone poi i province e poi le region
      context.stop(self)
    }

    private def tick(): Unit = {
      if (currentTime.hour == 0) {
        currentInfections = Statistic(controller.people).numCurrentPositive()
        simulation.updateInfectedCount(currentTime, currentInfections)
        println(s"Infection on ${currentTime.getTime}: $currentInfections")
        if (currentInfections > localMaxInfections) localMaxInfections = currentInfections
      }
      //checkLockdown(currentTime)
      //controller.tick(currentTime)
      println();println("---------------------------------------------------------------------------")
      _subordinatedActors.foreach(_ ! Tick(currentTime))
      context.setReceiveTimeout(Duration.create(100, TimeUnit.MILLISECONDS))
      currentTime = currentTime + 1
    }

    private[controller] def stopSimulation(): Unit = synchronized {
      actorsCoordinator ! Stop()
      //system.terminate()
    }

    private def checkLockdown(time: Calendar): Unit = {
      if (!lockdown && time >= nextAvailableLockdown && currentInfections > CovidInfectionParameters.lockDownStart * controller.people.size) {
        println("Start lockdown")
        controller.startLockdown(time, currentInfections)
        lockdown = true
        _subordinatedActors.foreach(_ ! Lockdown(lockdown))
      } else if (lockdown && currentInfections < CovidInfectionParameters.lockDownEnd * localMaxInfections) {
        println("End lockdown")
        nextAvailableLockdown = time ++ 30
        controller.endLockdown(time, currentInfections)
        localMaxInfections = 0
        lockdown = false
        _subordinatedActors.foreach(_ ! Lockdown(lockdown))
      }
    }

    private def createActors(regions: Seq[Region]): Unit = {
      val numRegion = regions.size //TODO farlo in base ai parametri di simulazione
      println(regions)
      val regionActors = regions.par.map {
        case region => system.actorOf(Props[RegionCoordinator]) -> region
      }.toMap

      //print(controller.provinces.size)
      println(s"Total regions considered in the simulation: $numRegion")

      regionActors.foreach({ case (actor, region) => actor ! SetRegion(region) })
      _subordinatedActors = regionActors.keySet
    }
  }


  /***
   * A Coordinator of the level 1. It manage a subSet of ProvinceCoordinator
   */
  class RegionCoordinator extends Actor with Coordinator {

    implicit protected  var _region: Region = _ // will be initialized later when the SetRegion message will be received
    private var _myProvinces: Seq[Province] = _

    override def receive: Receive = {
      case SetRegion(region) => this._region = region
        //this._myProvinces = controller.provinces.filter(p=>p.region==_region)
        //this.createActors(this._myProvinces)
      case Tick(currentTime) => this.busy(_region,currentTime)
      case ReceiveTimeout => /*actorsCoordinator ! Acknowledge();*/ println("WARNING: Timeout! Sono un sotto coordinatore: Region:"+_region)
    }

    private def createActors(provinces: Seq[Province]): Unit = {
      /*val numProvince = provinces.size //TODO farlo in base ai parametri di simulazione
      val provinceActors = provinces.par.map {
        case province => system.actorOf(Props[ProvinceCoordinator]) -> province
      }.toMap

      println(s"Total province considered in the simulation: $numProvince")

      provinceActors.foreach({ case (actor, province) => actor ! SetProvince(province,this) })
      _subordinatedActors = provinceActors.keySet*/
    }

    private def busy(region :Region, currentTime: Calendar) :Unit = { //esempio test
        println(region)
        context.setReceiveTimeout(Duration.create(80, TimeUnit.MILLISECONDS))
        Thread.sleep(20)
        //concluso avviso il livello superiore
        actorsCoordinator ! Acknowledge()
    }
  }

  /***
   * A Coordinator of the level 2. It manage a subSet of PersonActors
   */
  class ProvinceCoordinator extends Actor with Coordinator {
    implicit protected var _province: Province = _ // will be initialized later when the SetProvince message will be received
    private var _myPeople: Seq[Person] = _
    private var _upperCoordinator: RegionCoordinator = _ // Will be initialized later when the SetProvince message will be received
    override def receive: Receive = {
      case SetProvince(province, upperCoordinator) =>
        this._province = province; this._upperCoordinator=upperCoordinator
        this._myPeople = controller.people.filter(p=>p.residence.province==_province)
        /*this.createActors(this._myPeople)*/
      case Tick(currentTime) => this.busy(_province, currentTime)
      case ReceiveTimeout => /*_upperCoordinator.self ! Acknowledge();*/ println("WARNING: Timeout! Sono un sotto coordinatore: Province:" + _province)
    }

    /*private def createActors(people: Seq[Person]): Unit = {
      val numPerson = people.size //TODO farlo in base ai parametri di simulazione
      val personActors = people.par.map {
        case person => system.actorOf(Props[PersonActor]) -> person
      }.toMap

      println(s"Total person considered in the simulation: $numPerson")

      provinceActors.foreach({ case (actor, province) => actor ! SetProvince(province) })
      _subordinatedActors = provinceActors.keySet
    }*/

    private def busy(province: Province, currentTime: Calendar): Unit = { //esempio test
      println(province)
      context.setReceiveTimeout(Duration.create(60, TimeUnit.MILLISECONDS))
      Thread.sleep(20)
      //concluso avviso il livello superiore
      _upperCoordinator.self ! Acknowledge()
    }
  }
}
