package pps.covid_sim.controller.actors.coordinators

import java.util.Calendar

import akka.actor.{ActorRef, Props, ReceiveTimeout}
import pps.covid_sim.controller.actors.CoordinatorCommunication.SetProvince
import pps.covid_sim.controller.actors.coordinators.ActorsCoordination.{controller, system}
import pps.covid_sim.model.container.PlacesContainer.{getPlaces, placesInCityOrElseInProvince}
import pps.covid_sim.model.container.{PeopleContainer, PlacesContainer, TransportLinesContainer}
import pps.covid_sim.model.people.People.{Employed, Student, Worker}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication.{Acknowledge, ActorsFriendsMap, AddPlan, GetBusLines, GetPlacesInArea, GetTrainLines, HourTick, RequestedLines, RequestedPlaces, SetCovidInfectionParameters, SetPerson, Stop}
import pps.covid_sim.model.people.actors.{EmployedActor, StudentActor, UnemployedActor}
import pps.covid_sim.model.places.FreeTime.Disco
import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.Place
import pps.covid_sim.util.scheduling.Planning.CustomPlan
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.TimeIntervalsImplicits._
import pps.covid_sim.util.time.{DatesInterval, HoursInterval}

import scala.collection.parallel.ParSeq
import scala.util.Random

/** *
 * A Coordinator of the level 2. It manage a subSet of PersonActors
 */
//noinspection ActorMutableStateInspection
case class ProvinceCoordinator() extends Coordinator {
  implicit protected var _province: Province = _ // will be initialized later when the SetProvince message will be received
  private var _myPeople: ParSeq[Person] = _ // Will be initialized later when the SetProvince message will be received
  private var _upperCoordinator: ActorRef = _ // Will be initialized later when the SetProvince message will be received
  override def receive: Receive = {
    case SetProvince(province) =>
      this._province = province;
      this._upperCoordinator = sender
      this._myPeople = PeopleContainer.people(_province).par
      this.createActors(this._myPeople)
    case Acknowledge() if this.waitingAck.contains(sender) => this.waitingAck -= sender
      if (this.waitingAck.isEmpty) sendAck()
    case HourTick(currentTime) => this.spreadTick(currentTime)
    case ReceiveTimeout => sendAck()
    case Stop() => this.endSimulation()
    case GetPlacesInArea(city: City, placeClass, datesInterval) => this.genericGetPlaceByCity(city, placeClass, datesInterval, sender)
    case GetPlacesInArea(province: Province, placeClass, datesInterval) => this.genericGetPlaceByProvince(province, placeClass, datesInterval, sender)
    case GetBusLines(from, time) => sender ! RequestedLines(TransportLinesContainer.getBusLines(from, time))
    case GetTrainLines(from, time) => sender ! RequestedLines(TransportLinesContainer.getTrainLines(from, time))
    case msg => println(s"Not expected [Province]: $msg" + "is sender in peoples: " + waitingAck.contains(sender) + " " + sender.toString());
  }

  private def createActors(people: ParSeq[Person]): Unit = {
    val numPerson = people.size
    val discoProbability = 0.4
    val peopleActors = people.par.map {
      case student@Student(_, _) => system.actorOf(Props[StudentActor]) -> student
      case employed: Employed => system.actorOf(Props[EmployedActor]) -> employed
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
        case employed: Worker if employed.workPlace != null =>
          actor ! AddPlan(employed.workPlace.getWorkPlan(employed).get)
        case student: Student => if(student.institute != null && student.lesson != null)
            actor ! AddPlan(student.institute.getStudentPlan(student.lesson).get)
          if(Random.nextDouble() < discoProbability) {
            val interval: HoursInterval = 22 -> 4
            PlacesContainer.getPlaces.collectFirst({ case disco: Disco if (disco.city == student.residence ||
              disco.city.province == student.residence.province) &&
              disco.timeTable.isDefinedOn(Day.SATURDAY, interval) => disco
            }) match {
              case Some(disco: Disco) => actor ! AddPlan(CustomPlan(disco.openedInLockdown).add(disco, Day.SATURDAY, interval))
              case _ =>
            }
          }
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

  private def genericGetPlaceByProvince(province: Province, placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval], sender: ActorRef): Unit = {
    var res: List[Place] = List()
    if (datesInterval.isEmpty) {
      res = getPlaces(province, placeClass)
    } else if (datesInterval.isDefined) {
      res = getPlaces(province, placeClass, datesInterval.get)
    }
    sender ! RequestedPlaces(res)
  }

  private def genericGetPlaceByCity(city: City, placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval], sender: ActorRef): Unit = {
    var res: List[Place] = List()
    if (datesInterval.isEmpty) {
      res = getPlaces(city, placeClass)
    } else if (datesInterval.isDefined) {
      res = placesInCityOrElseInProvince(city, placeClass, datesInterval.get)
    }
    sender ! RequestedPlaces(res)
  }

  private def endSimulation(): Unit = {
    _subordinatedActors.foreach(s => s ! Stop())
    context.stop(self)
  }
}
