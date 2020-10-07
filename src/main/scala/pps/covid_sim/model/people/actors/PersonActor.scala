package pps.covid_sim.model.people.actors

import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication._
import pps.covid_sim.model.people.actors.Request.Request
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.Shops.SuperMarket
import pps.covid_sim.model.places.{Habitation, LimitedHourAccess, Place}
import pps.covid_sim.model.transports.PublicTransports.{BusLine, PublicTransport}
import pps.covid_sim.parameters.GoingOutParameters
import pps.covid_sim.parameters.GoingOutParameters.maxNumShopPerWeek
import pps.covid_sim.util.CommonPlacesByTime.randomPlaceWithPreferences
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.util.scheduling.{Agenda, Appointment}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, MonthsInterval}

import scala.collection.mutable.ListBuffer
import scala.util.Random

//noinspection ActorMutableStateInspection
abstract class PersonActor extends Actor {

  implicit protected var person: Person = _ // will be initialized later when the SetPerson message will be received

  protected lazy val maxGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
    .add(MonthsInterval.ALL_YEAR, DaysInterval.ALL_WEEK, 1)

  private var coordinator: ActorRef = _
  private var covidInfectionParameters: CovidInfectionParameters = _

  private lazy val agenda: Agenda = Agenda(person)

  private lazy val averageGoingOutFriends: Int = if (person.age < 40)
    RandomGeneration.randomIntInRange(1, 5) else
    RandomGeneration.randomIntInRange(1, 3)

  private lazy val maskProbability: Double = RandomGeneration.randomDoubleInRange(covidInfectionParameters.minMaskProbability,
    covidInfectionParameters.maxMaskProbability)

  private lazy val averageDistance: Double = RandomGeneration.randomDoubleFromGaussian(covidInfectionParameters
    .averageSocialDistance, 0.5, 0.1)

  private lazy val notRespectingIsolation: Double = RandomGeneration.randomDoubleInRange(0,
    covidInfectionParameters.notRespectingIsolationMaxProbability)

  private var time: Calendar = _
  private var inPandemic: Boolean = false
  private var lockdown: Boolean = false
  private var waitingResponses: ListBuffer[ActorRef] = ListBuffer()
  private var isOut = false
  private var friendsFound = false
  private var friends: Map[Person, ActorRef] = Map()
  private var wantsToGoOutToday: Boolean = false
  private var hasToDoShopping: Boolean = false
  private var currentCommitment: Option[(DatesInterval, Option[Location], Option[Group])] = None
  private lazy val placesPreferences = GoingOutParameters.placesPreferences(person.age)
  private val numShopPerWeek: Int = RandomGeneration.randomIntInRange(1, maxNumShopPerWeek)
  private val transportProbability = RandomGeneration.randomDoubleInRange(0, 0.8)
  private var pendingRequest: DatesInterval = _
  private var requestType: Request = _
  private var means: Option[PublicTransport] = None

  override def receive: Receive = {
    case SetPerson(person) => this.coordinator = sender; this.person = person
    case SetCovidInfectionParameters(covidInfectionParameters) => this.covidInfectionParameters = covidInfectionParameters
    case ActorsFriendsMap(friends) => this.friends = friends
    case HourTick(time) => this.time = time; person.hourTick(time); nextAction()
      if(means.isDefined) means = None
    case AddPlan(plan) => agenda.addPlan(plan)
    case RemovePlan(oldPlan) => agenda.removePlan(oldPlan)
    case Lockdown(enabled) => lockdown = enabled; if (!inPandemic) inPandemic = true
    case RequestedLines(lines: List[BusLine]) => waitingResponses = waitingResponses - coordinator
      sendAckIfReady()
      if(lines.nonEmpty) means = lines.iterator
        .map(_.tryUse(person, time))
        .find(_.isDefined)
        .flatten
    case p @ RequestedPlaces(places) => places match {
        case _ if requestType == Request.LOOKING_FOR_PLACES =>
          formulateAndSendProposal(pendingRequest, places)
        case _ if requestType == Request.LOOKING_FOR_MARKET => planShopping(places)
        case _ if places.nonEmpty => currentCommitment = Some(currentCommitment.get._1,
          Random.shuffle(places).head.enter(currentCommitment.get._3.get, currentCommitment.get._1.from),
          currentCommitment.get._3)
        case _ => println("ADVICE: No places found!")
      }
      waitingResponses -= coordinator
      sendAckIfReady()
    case p@GoOutProposal(_, _, _) if !mayAcceptProposal(p) => sender ! GoOutResponse(response = false, p)
    case p@GoOutProposal(dateInterval, place, _) if agenda.isFreeAt(p.datesInterval) =>
      sender ! GoOutResponse(response = true, p)
      agenda.joinAppointment(Appointment(dateInterval, place))
    case p@GoOutProposal(dateInterval, place, leader) =>
      val newInterval = agenda.firstNextFreeTime(dateInterval.from)(dateInterval.size)
      place match {
        case limited: LimitedHourAccess if limited.timeTable.isDefinedBetween(newInterval) =>
          sender ! GoOutResponse(response = false, p, GoOutProposal(newInterval.clipToTimeTable(limited.timeTable),
            place, leader))
        case _: LimitedHourAccess => sender ! GoOutResponse(response = false, p)
        case _ => sender ! GoOutResponse(response = false, p, GoOutProposal(newInterval, place, leader))
      }
    case msg: GoOutResponse /*if waitingResponses.contains(sender)*/ =>
      if ((msg.request.leader eq person) && waitingResponses.contains(sender)) {
        waitingResponses -= sender
        sendAckIfReady()
      }
      msg match {
        case GoOutResponse(response, request, _, from) if response && (request.leader eq person) =>
          goingOutConfirmed(request, from)
        case GoOutResponse(response, request, _, _) if response =>
          agenda.joinAppointment(Appointment(request.datesInterval, request.place))
        case GoOutResponse(response, _, newRequest, _) if !response && newRequest.isEmpty => // nothing to do :(
        case GoOutResponse(_, _, newRequest, from) if agenda.isFreeAt(newRequest.get.datesInterval) =>
          goingOutConfirmed(newRequest.get, from)
          sender ! GoOutResponse(response = true, newRequest.get)
        case GoOutResponse(_, _, newRequest, _) => sender ! GoOutResponse(response = false, newRequest.get)
      }
    case Stop() => context.stop(self)
  }

  private def sendAckIfReady(): Unit = {
    if(waitingResponses.isEmpty) coordinator ! Acknowledge()
  }

  private def goingOutConfirmed(goOutProposal: GoOutProposal, withPerson: Person): Unit = {
    friendsFound = true
    val appointment = Appointment(goOutProposal.datesInterval, goOutProposal.place)
    agenda.fixAppointment(appointment, Multiple(person, Set(person, withPerson)))(_ + withPerson)
  }

  private def nextAction(): Unit = {
    waitingResponses = ListBuffer()
    if (time.hour == 0) updateDaysParameters(time)
    agenda.removeAppointmentsEndedBefore(time)
    currentCommitment match {
      case Some((datesInterval, Some(location), group)) if datesInterval.until is time =>
        if(group.isDefined) location.exit(group.get) // only group leader will do that
        comeBack()
        nextCommitment()
      case None => nextCommitment()
      case _ =>
    }
    sendAckIfReady()
  }

  private def requestPlaces(area: Area, placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval] = None): Unit = {
    waitingResponses += coordinator
    coordinator ! GetPlacesInArea(area, placeClass, datesInterval)
  }

  private def nextCommitment(): Unit =
    currentCommitment = agenda.nextCommitment(time, lockdown) match {
      case commitment @ Some((datesInterval, location, Some(group))) if group.leader eq this.person => goOut()
        person.setMask(chooseMask(location))
        tryEnterInPlace(location, time, group) match {
          case Some(_) =>  commitment
          case _ => Some((datesInterval, None, Some(group)))
        }
      case commitment @ Some(_) => goOut()
        person.setMask(chooseMask(commitment.get._2))
        commitment
      case _ if time.hour > 8 && shopTime() => requestType = Request.LOOKING_FOR_MARKET
        val interval = agenda.firstNextFreeTime(time + 1)(2)
        requestPlaces(person.residence, classOf[SuperMarket], Some(interval))
        pendingRequest = interval
        None
      case _ if time.hour > 8 && mayGoOut() && !friendsFound => organizeGoingOut(time); None
      case _ => None
    }

  private def shopTime(): Boolean = (person eq person.habitation.leader) && hasToDoShopping

  private def planShopping(shops: List[Place]): Unit = {
    Random.shuffle(shops).headOption match {
      case Some(superMarket: SuperMarket) => hasToDoShopping = false
        agenda.fixAppointment(Appointment(pendingRequest.clipToTimeTable(superMarket.timeTable), superMarket), person)
      case _ => None
    }
  }

  private def comeBack(): Unit = {
    isOut = false
    person.habitation.enter(person, null)
    person.setMask(None)
    currentCommitment = None
  }

  private def goOut(): Unit = {
    chooseTransport()
    person.socialDistance = RandomGeneration.randomDoubleFromGaussian(averageDistance, 0.5, 0.1)
    isOut = true
    person.habitation.exit(person)
  }

  private def chooseMask(location: Location): Option[Mask] = if(inPandemic && !location.isInstanceOf[Habitation])
    location.mask match {
      case mask @ Some(_) if Random.nextDouble() < maskProbability => mask
      case _ if Random.nextDouble() < maskProbability => Some(Masks.Surgical)
      case _ => None
    } else None

  private def tryEnterInPlace(location: Location, time: Calendar, group: Group): Option[Location] = {
    location match {
      case place: Place => place.enter(group, time) match {
        case location @ Some(_) => location
        case _ => requestType = Request.LOOKING_FOR_ALTERNATIVE; randomOpenPlaces(); None
      }
      case _ => location.enter(group, time)
    }
  }

  private def randomOpenPlaces(): Unit = requestPlaces(person.residence, classOf[OpenPlace])

  private def organizeGoingOut(time: Calendar): Unit = {
    val interval = agenda.firstNextFreeTime(time + 1)
    randomPlaceWithPreferences(placesPreferences, interval) match {
      case Some(placeClass) if !lockdown || !covidInfectionParameters.placesToClose.contains(placeClass) =>
        requestPlaces(person.residence, placeClass, Some(interval))
        requestType = Request.LOOKING_FOR_PLACES
        pendingRequest = interval
      case _ =>
    }
  }

  private def formulateAndSendProposal(datesInterval: DatesInterval, places: List[Place]): Unit = places match {
    case Nil => None
    case places: List[Place] => var interval = datesInterval
      val goingOut = (Random.shuffle(places).head match {
        case limitedPlace: LimitedHourAccess => interval = datesInterval.clipToTimeTable(limitedPlace.timeTable)
          limitedPlace
        case place: Place => place
      }, interval.limits(RandomGeneration.randomIntInRange(1, 6)))
      randomFriends.foreach(actor => {
        waitingResponses += actor
        actor ! GoOutProposal(datesInterval, goingOut._1, person)
      })
  }

  private def updateDaysParameters(time: Calendar): Unit = {
    friendsFound = false
    hasToDoShopping = Random.nextDouble() < 1.0 / numShopPerWeek
    wantsToGoOutToday = Random.nextDouble() < maxGoingOutTimes.probability(time.month, time.day)
  }

  private def mayAcceptProposal(goOutProposal: GoOutProposal): Boolean = mayGoOut() &&
    wantsToGoOutWith(goOutProposal.leader, goOutProposal.place)

  private def mayGoOut(): Boolean = wantsToGoOutToday && (!person.isInfected ||
    Random.nextDouble() < notRespectingIsolation)

  private def wantsToGoOutWith(person: Person, at: Place): Boolean = {
    var probability: Double = if (this.person.friends.contains(person)) {
      RandomGeneration.randomDoubleInRange(0.8)
    } else {
      RandomGeneration.randomDoubleInRange(0.5, 0.8)
    }
    probability = probability + (1 - probability) * placesPreferences.getOrElse(at.getClass, 0.0)
    new Random().nextFloat() < probability
  }

  private def chooseTransport(): Unit = {
    if(Random.nextDouble() < transportProbability) {
      waitingResponses += coordinator
      coordinator ! GetBusLines(person.residence, time)
      //GetTrainLines(person.residence, time)
    }
  }

  private def randomFriends: Set[ActorRef] = Random.shuffle(person.friends)
    .take(RandomGeneration.randomIntFromGaussian(averageGoingOutFriends, 3, 1))
    .map(friends)

  private implicit def optionalTuple(optional:
                                     Option[(DatesInterval,
                                       Location,
                                       Option[Group])]): Option[(DatesInterval, Option[Location], Option[Group])] = {
    optional.map(op => (op._1, Some(op._2), op._3))
  }

}
