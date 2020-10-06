package pps.covid_sim.model.people.actors

import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.container.PlacesContainer
import pps.covid_sim.model.container.PlacesContainer.placesInCityOrElseInProvince
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication._
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.Shops.SuperMarket
import pps.covid_sim.model.places.{Habitation, LimitedHourAccess, Place}
import pps.covid_sim.model.samples.Places
import pps.covid_sim.parameters.GoingOutParameters
import pps.covid_sim.parameters.GoingOutParameters.maxNumShopPerWeek
import pps.covid_sim.util.CommonPlacesByTime.randomPlaceWithPreferences
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.util.scheduling.{Agenda, Appointment}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, MonthsInterval}

import scala.annotation.tailrec
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

  private lazy val notRespectingIsolation: Double = RandomGeneration.randomDoubleInRange(0,
    covidInfectionParameters.notRespectingIsolationMaxProbability)

  private var inPandemic: Boolean = false
  private var lockdown: Boolean = false
  private var waitingResponses: Set[ActorRef] = Set()
  private var isOut = false
  private var friendsFound = false
  private var friends: Map[Person, ActorRef] = Map()
  private var wantsToGoOutToday: Boolean = false
  private var hasToDoShopping: Boolean = false
  private var currentCommitment: Option[(DatesInterval, Location, Option[Group])] = None
  private lazy val placesPreferences = GoingOutParameters.placesPreferences(person.age)
  private val numShopPerWeek: Int = RandomGeneration.randomIntInRange(1, maxNumShopPerWeek)
  private implicit val actorName: String = self.toString()

  override def receive: Receive = {
    case SetPerson(person) => this.coordinator = sender; this.person = person
    case SetCovidInfectionParameters(covidInfectionParameters) => this.covidInfectionParameters = covidInfectionParameters
    case ActorsFriendsMap(friends) => this.friends = friends
    case HourTick(time) => person.hourTick(time); nextAction(time)
    case AddPlan(plan) => agenda.addPlan(plan)
    case RemovePlan(oldPlan) => agenda.removePlan(oldPlan)
    case Lockdown(enabled) => lockdown = enabled; if(!inPandemic) inPandemic = true
    case p @ GoOutProposal(_, _, _) if !mayAcceptProposal(p) => sender ! GoOutResponse(response = false, p)
    case p @ GoOutProposal(dateInterval, place, _) if agenda.isFreeAt(p.datesInterval) =>
      sender ! GoOutResponse(response = true, p)
      agenda.joinAppointment(Appointment(dateInterval, place))
    case p @ GoOutProposal(dateInterval, place, leader) =>
      val newInterval = agenda.firstNextFreeTime(dateInterval.from)(dateInterval.size)
      place match {
        case limited: LimitedHourAccess if limited.timeTable.isDefinedBetween(newInterval) =>
          sender ! GoOutResponse(response = false, p, GoOutProposal(newInterval.clipToTimeTable(limited.timeTable),
            place, leader))
        case _: LimitedHourAccess => sender ! GoOutResponse(response = false, p)
        case _ => sender ! GoOutResponse(response = false, p, GoOutProposal(newInterval, place, leader))
      }
    case msg: GoOutResponse /*if waitingResponses.contains(sender)*/ =>
      if((msg.request.leader eq person) && waitingResponses.contains(sender)){
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
  }

  private def sendAckIfReady(): Unit = {
    if(waitingResponses.isEmpty) coordinator ! Acknowledge()
  }

  private def goingOutConfirmed(goOutProposal: GoOutProposal, withPerson: Person): Unit = {
    friendsFound = true
    val appointment = Appointment(goOutProposal.datesInterval, goOutProposal.place)
    agenda.fixAppointment(appointment, Multiple(person, Set(person, withPerson)))(_ + withPerson)
  }

  private def nextAction(time: Calendar): Unit = {
    waitingResponses = Set()
    if (time.hour == 0) updateDaysParameters(time)
    agenda.removeAppointmentsEndedBefore(time)

    currentCommitment match {
      case Some((datesInterval, location, group)) if datesInterval.until is time =>
        if(group.isDefined) location.exit(group.get) // only group leader will do that
        comeBack()
        nextCommitment(time)
      case None => nextCommitment(time)
      case _ =>
    }
    sendAckIfReady()
  }

  private def nextCommitment(time: Calendar): Unit = {
    currentCommitment = agenda.nextCommitment(time) match {
      case Some((datesInterval, location, Some(group))) if group.leader eq this.person => goOut()
        person.setMask(chooseMask(location))
        Some(datesInterval, tryEnterInPlace(location, time, group), Some(group))
      case commitment@Some(_) => goOut()
        person.setMask(chooseMask(commitment.get._2))
        commitment
      case _ if time.hour > 8 && shopTime() => goShopping(time); None
      case _ if time.hour > 8 && mayGoOut() && !friendsFound => organizeGoingOut(time); None
      case _ => None
    }
  }

  private def shopTime(): Boolean = (person eq person.habitation.leader) && hasToDoShopping

  private def goShopping(time: Calendar): Unit = {
    val interval = agenda.firstNextFreeTime(time + 1)(2)
    Random.shuffle(
      PlacesContainer.placesInCityOrElseInProvince(person.residence, classOf[SuperMarket], interval)).headOption match {
      case Some(superMarket: SuperMarket) => hasToDoShopping = false
        agenda.fixAppointment(Appointment(interval.clipToTimeTable(superMarket.timeTable), superMarket), person)
      case _ => None
    }
  }

  private def comeBack(): Unit = {
    // TODO autobus
    isOut = false
    person.habitation.enter(person, null)
    person.setMask(None)
    currentCommitment = None
  }

  private def goOut(): Unit = {
    // TODO autobus
    isOut = true
    person.habitation.exit(person)
  }

  private def chooseMask(location: Location): Option[Mask] = if(inPandemic && !location.isInstanceOf[Habitation])
    location.mask match {
      case mask @ Some(_) /*if Random.nextDouble() < maskProbability*/ => mask
      case _ if Random.nextDouble() < maskProbability => Some(Masks.Surgical)
      case _ => None
    } else None

  @tailrec
  private def tryEnterInPlace(location: Location, time: Calendar, group: Group, maxAlternatives: Int = 3): Location = {
    location match {
      case place: Place => place.enter(group, time) match {
        case Some(location) => location
        case None if maxAlternatives > 0 => placesInCityOrElseInProvince(person.residence, place.getClass, time) match {
          case Nil => randomOpenPlace.enter(group, time).get // will always have success
          case places: List[Place] => tryEnterInPlace(Random.shuffle(places).head, time, group, maxAlternatives - 1)
        }
        case _ => randomOpenPlace.enter(group, time).get // will always have success
      }
      case _ => location.enter(group, time) match { // if location is in a plan, it will always have success
        case Some(value) =>  value
        case _ => println(s"WARNING: you can't enter in ${location.getClass.getSimpleName} at ${time.getTime}"); location
      }
    }
  }

  private def randomOpenPlace: Place = PlacesContainer.getPlaces(person.residence, classOf[OpenPlace]).headOption match {
    case Some(place) => place
    case _ => Places.PARK
  }

  private def organizeGoingOut(time: Calendar): Option[(Place, DatesInterval)] = {
    var interval = agenda.firstNextFreeTime(time + 1)
    randomPlaceWithPreferences(placesPreferences, interval) match {
      case Some(placeClass) => placesInCityOrElseInProvince(person.residence, placeClass, interval) match {
        case Nil => None
        case places: List[Place] => val goingOut = (Random.shuffle(places).head match {
          case limitedPlace: LimitedHourAccess => interval = interval.clipToTimeTable(limitedPlace.timeTable)
            limitedPlace
          case place: Place => place
        }, interval.limits(RandomGeneration.randomIntInRange(1, 6)))
          sendProposal(goingOut._2, goingOut._1, randomFriends)
          Some(goingOut)
      }
      case _ => None
    }
  }

  private def sendProposal(datesInterval: DatesInterval, place: Place, receivers: Iterable[ActorRef]): Unit = {
    receivers.foreach(actor => {
      waitingResponses += actor
      actor ! GoOutProposal(datesInterval, place, person)
    })
  }

  private def updateDaysParameters(time: Calendar): Unit = {
    friendsFound = false
    hasToDoShopping = Random.nextDouble() < 1.0 / numShopPerWeek
    wantsToGoOutToday = Random.nextDouble() < maxGoingOutTimes.probability(time.month, time.day)
  }

  private def mayAcceptProposal(goOutProposal: GoOutProposal): Boolean = mayGoOut() &&
    wantsToGoOutWith(goOutProposal.leader, goOutProposal.place)

  private def mayGoOut(): Boolean = wantsToGoOutToday && (!(lockdown || person.isInfected) ||
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

  private def randomFriends: Set[ActorRef] = Random.shuffle(person.friends)
    .take(RandomGeneration.randomIntFromGaussian(averageGoingOutFriends, 3, 1))
    .map(friends)


}
