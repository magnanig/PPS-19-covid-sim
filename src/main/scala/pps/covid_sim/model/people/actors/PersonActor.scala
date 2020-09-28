package pps.covid_sim.model.people.actors

import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.people.actors.Communication._
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.{LimitedHourAccess, Place}
import pps.covid_sim.parameters.CovidInfectionParameters.notRespectingIsolationMaxProbability
import pps.covid_sim.parameters.GoingOutParameters
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.scheduling.GoingOutTimes.{GoingOutTimes, GoingOutTimesMap}
import pps.covid_sim.util.scheduling.{Agenda, Appointment}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, MonthsInterval}

import scala.util.Random

//noinspection ActorMutableStateInspection
abstract class PersonActor extends Actor {

  implicit protected var person: Person = _ // will be initialized later when the SetPerson message will be received

  protected lazy val maxGoingOutTimes: GoingOutTimes = GoingOutTimesMap()
    .add(MonthsInterval.ALL_YEAR, DaysInterval.ALL_WEEK, 1)

  private var coordinator: ActorRef = _

  private lazy val agenda: Agenda = Agenda(person)

  private val notRespectingIsolation: Double = RandomGeneration.randomDoubleInRange(0,
    notRespectingIsolationMaxProbability)

  private var inPandemic: Boolean = false
  private var lockdown: Boolean = false
  private var waitingResponses: Set[ActorRef] = Set()
  private var isOut = false
  private var friendsFound = false
  private var friends: Map[Person, ActorRef] = Map()
  private var wantsToGoOutToday: Boolean = false
  private var currentCommitment: Option[(DatesInterval, Location, Option[Group])] = None
  private lazy val placesPreferences = GoingOutParameters.placesPreferences(person.age)
  private implicit val actorName: String = self.toString()

  override def receive: Receive = {
    case SetPerson(person) => this.coordinator = sender; this.person = person
    case ActorsFriendsMap(friends) => this.friends = friends
    case HourTick(time) => person.hourTick(time); nextAction(time)
    case AddPlan(plan) => agenda.addPlan(plan)
    case ReplacePlan(oldPlan) => agenda.removePlan(oldPlan)
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

  private def nextCommitment(time: Calendar): Unit = ???

  private def comeBack(): Unit = {
    isOut = false
    person.habitation.enter(person, null)
    person.setMask(None)
    currentCommitment = None
  }

  private def updateDaysParameters(time: Calendar): Unit = {
    friendsFound = false
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

}
