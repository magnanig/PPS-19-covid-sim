package pps.covid_sim.controller.actors

import java.util.Calendar

import akka.actor.{ActorRef, ActorSystem, Props, ReceiveTimeout}
import pps.covid_sim.controller.Controller
import pps.covid_sim.controller.actors.CoordinatorCommunication.{SetProvince, SetRegion}
import pps.covid_sim.model.container.PeopleContainer
import pps.covid_sim.model.container.PlacesContainer.getPlaces
import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication._
import pps.covid_sim.model.people.actors.{StudentActor, UnemployedActor, WorkerActor}
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.util.Statistic
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq
import scala.collection.parallel.immutable.ParSet

object ActorsCoordination {

  private[actors] case class Init(area: Area, controller: Controller, datesInterval: DatesInterval)

  private[actors] var system: ActorSystem = _
  private[actors] var actorsCoordinator: ActorRef = _

  private var controller: Controller = _
  private var simulationInterval: DatesInterval = _
  private var currentTime: Calendar = _
  private var simulationArea: Area = _

  /**
   * method that allow to create all the hierarchy of coordinators
   * @param controller that coordinators call to get useful data and run methods
   * @param datesInterval that indicates the interval the user had insert for the simulation
   */
  private[controller] def create(area: Area, controller: Controller, datesInterval: DatesInterval): Unit = {
    println("Going to create actors")
    this.controller = controller
    system = ActorSystem.create()
    actorsCoordinator = system.actorOf(Props[ActorsCoordinator])
    simulationArea = area
    actorsCoordinator ! Init(area, controller, datesInterval)
  }
  /***
   * The main Coordinator of level 0. It manage a subSet of RegionCoordinator
   */
  class ActorsCoordinator extends Coordinator {

    private var localMaxInfections: Int = 0
    private var nextAvailableLockdown: Calendar = _
    private var lockdown: Boolean = false


    override def receive: Receive = {
      case Init(area, c, di) => //controller = c
        area match {
          //case City(idCity, name, numResidents, province, latitude, longitude) => _//non gestita
          case province: Province => this.createProvinceActors(province) //Set()//voglio creare una region contenente la sola provincia da analizzare
          case region: Region => this.createActors(Set(region))//solo la singola regione
          case Locality.Italy() => this.createActors(c.regions)//tutte le regioni
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
      println();println("----->Tick<-----")

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
      //val numRegion = regions.size

      /*val regionActors = regions.par.collect {
        case region if controller.people.count(_.residence.province.region == region) > 0 => system.actorOf(Props[RegionCoordinator]) -> region
      }.toMap*/

      val regionActors = controller.people.groupBy(_.residence.province.region).map(entry => {
        println(s"TOPC: Total people considered in the simulation: ${entry._2.size} "+ "coordinatore principale crea: " + entry._1.name)
        val actor = system.actorOf(Props[RegionCoordinator])
        actor ! SetRegion(entry._1)
        actor -> entry._1
      })

      _subordinatedActors = regionActors.keySet.toSet
      //println(s"Total regions considered in the simulation: ${regionActors.size}"+ "coordinatore principale crea: " + _subordinatedActors)
      this.waitingAck = _subordinatedActors
      //regionActors.foreach({ case (actor, region) => actor ! SetRegion(region) })
    }

    def createProvinceActors(province: Province):Unit = {
      val provinceActor = system.actorOf(Props[ProvinceCoordinator])
      this._subordinatedActors = ParSet(provinceActor)
      this.waitingAck = _subordinatedActors
      provinceActor ! SetProvince(province)
    }
  }

  /***
   * A Coordinator of the level 1. It manage a subSet of ProvinceCoordinator
   */
  class RegionCoordinator extends Coordinator {

    implicit protected  var _region: Region = _ // will be initialized later when the SetRegion message will be received
    private var _myProvinces: Set[Province] = _ // Will be initialized later when the SetProvince message will be received

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

    private def createActors(provinces: Set[Province]): Unit = {
      //val numProvince = provinces.size
      /*val provinceActors = provinces.par.collect {
        case province if controller.people.count(_.residence.province == province) > 0 => system.actorOf(Props[ProvinceCoordinator]) -> province
      }.toMap*/

      val provinceActors = controller.people.groupBy(_.residence.province).map(entry => {
        println(s"REGC: Total people considered in the simulation: ${entry._2.size} "+ "of region: "+ entry._1)
        val actor = system.actorOf(Props[ProvinceCoordinator])
        actor ! SetProvince(entry._1)
        actor -> entry._1
      })

      this._subordinatedActors = provinceActors.keySet.toSet
      this.waitingAck = _subordinatedActors

      //provinceActors.foreach({ case (actor, province) => actor ! SetProvince(province) })
    }

    private def sendAck(): Unit = {
      this.waitingAck = _subordinatedActors
      actorsCoordinator ! Acknowledge()
    }

    private def spreadTick(region :Region, currentTime: Calendar) :Unit = { //esempio test
      println(region)
      this.waitingAck = _subordinatedActors
      //context.setReceiveTimeout(Duration.create(50, TimeUnit.MILLISECONDS))
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
  class ProvinceCoordinator extends Coordinator {
    implicit protected var _province: Province = _ // will be initialized later when the SetProvince message will be received
    private var _myPeople: ParSeq[Person] = _ // Will be initialized later when the SetProvince message will be received
    private var _upperCoordinator: ActorRef = _ // Will be initialized later when the SetProvince message will be received
    override def receive: Receive = {
      case SetProvince(province) =>
        this._province = province;  this._upperCoordinator = sender
        this._myPeople = PeopleContainer.getPeople(_province).par
        //this._myPeople = controller.people.filter(p=>p.residence.province==_province)
        this.createActors(this._myPeople)
      case Acknowledge() if this.waitingAck.contains(sender) => this.waitingAck -= sender
        if (this.waitingAck.isEmpty) {println("Sending CORRECT cumulative ACK");sendAck()}
      case HourTick(currentTime) => this.spreadTick(currentTime)
      case ReceiveTimeout => println("Sending INCORRECT cumulative ACK");sendAck(); println("WARNING: Timeout! Sono un sotto coordinatore: Province:" + _province + "size: " +waitingAck.size)
      case Stop() => this.endSimulation()
      case GetPlacesByProvince(province, placeClass, datesInterval) => this.genericGetPlaceByProvince(province, placeClass, datesInterval,sender)
      case GetPlacesByCity(city, placeClass, datesInterval) => this.genericGetPlaceByCity(city, placeClass, datesInterval,sender)
      case msg => println(s"Not expected [Province]: $msg" +"is sender in peoples: "+waitingAck.contains(sender) +" "+ sender.toString());
    }

    private def createActors(people: ParSeq[Person]): Unit = {
      val numPerson = people.size
      var numWorker = 0
      val peopleActors = people.par.map {
        case student@Student(_, _) => system.actorOf(Props[StudentActor]) -> student
        case worker@Worker(_, _) => numWorker = numWorker + 1; system.actorOf(Props[WorkerActor]) -> worker
        case person => system.actorOf(Props[UnemployedActor]) -> person
      }.toMap

      println(s"PROC: Total people considered in the simulation: $numPerson")
      this._subordinatedActors = peopleActors.keySet.toSet
      this.waitingAck = _subordinatedActors

      peopleActors.foreach({ case (actor, person) =>
        actor ! SetPerson(person)
        actor ! SetCovidInfectionParameters(controller.covidInfectionParameters)
        actor ! ActorsFriendsMap(peopleActors.collect({ case (a, p) if person.friends.contains(p) => p -> a }).seq)
        person match {
          case worker: Worker if worker.workPlace == null => numWorker = numWorker + 1
          case worker: Worker => actor ! AddPlan(worker.workPlace.getWorkPlan(worker).get)
          case student: Student if student.institute != null && student.lesson != null =>
            actor ! AddPlan(student.institute.getStudentPlan(student.lesson).get)
          case _ =>
        }
      })
    }

    private def sendAck(): Unit = {

      this.waitingAck = _subordinatedActors
      this._upperCoordinator ! Acknowledge()
    }

    private def spreadTick(currentTime: Calendar): Unit = { //esempio test
      this._subordinatedActors.foreach(s => s ! HourTick(currentTime))
      this.waitingAck = _subordinatedActors
      //context.setReceiveTimeout(Duration.create(20, TimeUnit.MILLISECONDS))
    }

    private def genericGetPlaceByProvince(province: Province,placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval],sender: ActorRef):Unit = {
      var res:List[Place] = List()
      if(datesInterval.isEmpty){
        res = getPlaces(province, placeClass)
      }else if(datesInterval.isDefined) {
        res = getPlaces(province, placeClass, datesInterval.get)
      }
      sender ! RequestedPlaces(res)
    }

    private def genericGetPlaceByCity(city: City,placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval],sender: ActorRef):Unit = {
      var res:List[Place] = List()
      if(datesInterval.isEmpty){
        res = getPlaces(city, placeClass)
      }else if(datesInterval.isDefined) {
        res = getPlaces(city, placeClass, datesInterval.get)
      }
      sender ! RequestedPlaces(res)
    }

    private def endSimulation():Unit = {
      _subordinatedActors.foreach(s => s ! Stop())
      context.stop(self)
    }
  }
}
