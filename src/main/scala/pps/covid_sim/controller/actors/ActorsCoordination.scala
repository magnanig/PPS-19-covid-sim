package pps.covid_sim.controller.actors

import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props, ReceiveTimeout}
import pps.covid_sim.controller.ControllerImpl
import pps.covid_sim.controller.actors.CoordinatorCommunication.{SetProvince, SetRegion}
import pps.covid_sim.model.container.PlacesContainer.getPlaces
import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication._
import pps.covid_sim.model.people.actors.{StudentActor, UnemployedActor, WorkerActor}
import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.parameters.CovidInfectionParameters
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq
import scala.concurrent.duration.Duration

object ActorsCoordination {

  private[actors] case class Init(controller: ControllerImpl, datesInterval: DatesInterval)

  private[actors] var system: ActorSystem = _
  private[actors] var actorsCoordinator: ActorRef = _

  private var controller: ControllerImpl = _
  private var simulationInterval: DatesInterval = _
  private var simulation: Simulation = _
  private var currentTime: Calendar = _

  /**
   * method that allow to create all the hierarchy of coordinators
   * @param controller that coordinators call to get useful data and run methods
   * @param datesInterval that indicates the interval the user had insert for the simulation
   */
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

    override def receive: Receive = {
      case Init(c, di) => controller = c
        this.createActors(c.regions)
        simulationInterval = di
        //simulation = Simulation(c.people)
        currentTime = di.from
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
      //simulation.close()
      controller.simulationEnded(simulation)
      _subordinatedActors.foreach(s => s ! Stop())//TODO stoppare prima le persone poi i province e poi le region
      context.stop(self)
    }

    private def tick(): Unit = {
      if (currentTime.hour == 0) {
        currentInfections = Statistic(controller.people).numCurrentPositive()
        //simulation.updateInfectedCount(currentTime, currentInfections)
        println(s"Infection on ${currentTime.getTime}: $currentInfections")
        if (currentInfections > localMaxInfections) localMaxInfections = currentInfections
      }
      //checkLockdown(currentTime)
      //controller.tick(currentTime)
      println();println("---------------------------------------------------------------------------")
      _subordinatedActors.foreach(_ ! HourTick(currentTime))
      context.setReceiveTimeout(Duration.create(100, TimeUnit.MILLISECONDS))
      currentTime = currentTime + 1
    }

    private[controller] def stopSimulation(): Unit = synchronized {
      actorsCoordinator ! Stop()
      system.terminate()
    }

    private def checkLockdown(time: Calendar): Unit = {//TODO gestire il messaggio lockdown per i coordinatori figli!
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
      println(s"Total regions considered in the simulation: $numRegion")

      _subordinatedActors = regionActors.keySet
      this.waitingAck = _subordinatedActors

      regionActors.foreach({ case (actor, region) => actor ! SetRegion(region) })
    }
  }


  /***
   * A Coordinator of the level 1. It manage a subSet of ProvinceCoordinator
   */
  class RegionCoordinator extends Actor with Coordinator {

    implicit protected  var _region: Region = _ // will be initialized later when the SetRegion message will be received
    private var _myProvinces: Seq[Province] = _ // Will be initialized later when the SetProvince message will be received

    override def receive: Receive = {
      case SetRegion(region) => this._region = region
        this._myProvinces = controller.provinces.filter(p=>p.region==_region)
        this.createActors(this._myProvinces)
      case HourTick(currentTime) => this.spreadTick(_region,currentTime)
      case Acknowledge() if this.waitingAck.contains(sender) => this.waitingAck -= sender
        if (this.waitingAck.isEmpty) sendAck()
      case ReceiveTimeout => sendAck(); println("WARNING: Timeout! Sono un sotto coordinatore: Region:"+_region)
      case Stop() => this.endSimulation()
      case msg => println(s"Not expected [Region]: $msg")
    }

    private def createActors(provinces: Seq[Province]): Unit = {
      val numProvince = provinces.size //TODO farlo in base ai parametri di simulazione
      val provinceActors = provinces.par.map {
        case province => system.actorOf(Props[ProvinceCoordinator]) -> province
      }.toMap
      println(s"Total province considered in the simulation: $numProvince")
      this._subordinatedActors = provinceActors.keySet
      this.waitingAck = _subordinatedActors

      provinceActors.foreach({ case (actor, province) => actor ! SetProvince(province,this) })
    }

    private def sendAck(): Unit = {
      this.waitingAck = _subordinatedActors
      actorsCoordinator ! Acknowledge()
    }

    private def spreadTick(region :Region, currentTime: Calendar) :Unit = { //esempio test
      // println(region)
      this.waitingAck = _subordinatedActors
      context.setReceiveTimeout(Duration.create(80, TimeUnit.MILLISECONDS))
      this._subordinatedActors.foreach(s => s ! HourTick(currentTime))
    }

    private def endSimulation():Unit = {
      _subordinatedActors.foreach(s => s ! Stop())
      context.stop(self)
    }
  }

  /***
   * A Coordinator of the level 2. It manage a subSet of PersonActors
   */
  class ProvinceCoordinator extends Actor with Coordinator {
    implicit protected var _province: Province = _ // will be initialized later when the SetProvince message will be received
    private var _myPeople: ParSeq[Person] = _ // Will be initialized later when the SetProvince message will be received
    private var _upperCoordinator: RegionCoordinator = _ // Will be initialized later when the SetProvince message will be received
    override def receive: Receive = {
      case SetProvince(province, upperCoordinator) =>
        this._province = province;  this._upperCoordinator=upperCoordinator
        this._myPeople = controller.people.filter(p=>p.residence.province==_province)
        this.createActors(this._myPeople)
      case Acknowledge() if this.waitingAck.contains(sender) => this.waitingAck -= sender
        if (this.waitingAck.isEmpty) sendAck()
      case HourTick(currentTime) => this.spreadTick(_province, currentTime)
      case ReceiveTimeout => sendAck(); println("WARNING: Timeout! Sono un sotto coordinatore: Province:" + _province)
      case Stop() => this.endSimulation()
      case GetPlacesByProvince(province: Province, placeClass, datesInterval) => this.genericGetPlaceByProvince(province, placeClass, datesInterval,sender)
      case GetPlacesByCity(city: City, placeClass, datesInterval) => this.genericGetPlaceByCity(city, placeClass, datesInterval,sender)
      case msg => println(s"Not expected [Province]: $msg")
    }

    private def createActors(people: ParSeq[Person]): Unit = {
      val numPerson = people.size //TODO farlo in base ai parametri di simulazione
      var numWorker = 0
      val peopleActors = people.par.map {
        case student@Student(_, _) => system.actorOf(Props[StudentActor]) -> student
        case worker@Worker(_, _) => numWorker = numWorker + 1; system.actorOf(Props[WorkerActor]) -> worker
        case person => system.actorOf(Props[UnemployedActor]) -> person
      }.toMap

      println(s"Total person considered in the simulation: $numPerson")
      this._subordinatedActors = peopleActors.keySet
      this.waitingAck = _subordinatedActors

      peopleActors.foreach({ case (actor, person) => actor ! SetPerson(person) })

    }

    private def sendAck(): Unit = {
      this.waitingAck = _subordinatedActors
      this._upperCoordinator.self ! Acknowledge()
    }

    private def spreadTick(province: Province, currentTime: Calendar): Unit = { //esempio test
      //println(province)
      this.waitingAck = _subordinatedActors
      context.setReceiveTimeout(Duration.create(60, TimeUnit.MILLISECONDS))

      this._subordinatedActors.foreach(s => s ! HourTick(currentTime))
    }

    private def genericGetPlaceByProvince(province: Province,placeClass: Option[Class[_ <: Place]], datesInterval: Option[DatesInterval],sender: ActorRef):Unit = {
      var res:List[Place] = List()
      if(datesInterval.isEmpty  && placeClass.isDefined){
        res = getPlaces(province, placeClass.get)
      }else if(datesInterval.isDefined && placeClass.isDefined) {
        res = getPlaces(province, placeClass.get, datesInterval.get)
      }
      sender ! RequestedPlaces(res)
    }

    private def genericGetPlaceByCity(city: City,placeClass: Option[Class[_ <: Place]], datesInterval: Option[DatesInterval],sender: ActorRef):Unit = {
      var res:List[Place] = List()
      if(datesInterval.isEmpty  && placeClass.isDefined){
        res = getPlaces(city, placeClass.get)
      }else if(datesInterval.isDefined && placeClass.isDefined) {
        res = getPlaces(city, placeClass.get, datesInterval.get)
      }
      sender ! RequestedPlaces(res)
    }

    private def endSimulation():Unit = {
      _subordinatedActors.foreach(s => s ! Stop())
      context.stop(self)
    }
  }
}
