package pps.covid_sim.people.actors

import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.{LimitedHourAccess, Place}
import pps.covid_sim.people.actors.Communication._
import pps.covid_sim.util.scheduling.{Agenda, Appointment}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.util.time.{DatesInterval, DaysInterval, MonthsInterval}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.parameters.{CovidInfectionParameters, CreationParameters}

import scala.annotation.tailrec
import scala.util.Random

//noinspection ActorMutableStateInspection
abstract class PersonActor extends Actor {

  implicit protected var person: Person = _ // will be initialized later when the SetPerson message will be received

  //protected lazy val maxGoingOutTimes

  private var coordinator: ActorRef = _

  private lazy val agenda: Agenda = Agenda(person)

  private lazy val averageGoingOutFriends: Int = if (person.age < 40)
    RandomGeneration.randomIntInRange(1, 5) else
    RandomGeneration.randomIntInRange(1, 3)

  private val maskProbability: Double = RandomGeneration.randomDoubleInRange(CovidInfectionParameters.minMaskProbability,
    CovidInfectionParameters.maxMaskProbability)

  private val notRespectingIsolation: Double = RandomGeneration.randomDoubleInRange(0,
    CovidInfectionParameters.notRespectingIsolationMaxProbability)

  private var inPandemic: Boolean = false
  private var lockdown: Boolean = false
  private var waitingResponses: Set[ActorRef] = Set()
  private var isOut = false
  private var friendsFound = false
  private var friends: Map[Person, ActorRef] = Map()
  private var wantsToGoOutToday: Boolean = Random.nextBoolean()
  private var currentCommitment: Option[(DatesInterval, Location, Option[Group])] = None
  //private lazy val placesPreferences = Parameters.placesPreferences(person.age)
  private implicit val actorName: String = self.toString()

  override def receive: Receive = {
    case SetPerson(person) => this.coordinator = sender; this.person = person;
    case ActorsFriendsMap(friends) => this.friends = friends
    case Tick(time) =>  this.busy()//TODO its a test
    //case AddPlan(plan)
    //case ReplacePlan(oldPlan, null)
    //case ReplacePlan(oldPlan, newPlan)
    case Lockdown(enabled) => lockdown = enabled; if(!inPandemic) inPandemic = true
    //case p @ GoOutProposal(_, _, _) if !mayAcceptProposal(p) => sender ! GoOutResponse(response = false, p)
    /*case p @ GoOutProposal(dateInterval, place, _) if agenda.isFreeAt(p.dateInterval) =>
      sender ! GoOutResponse(response = true, p)
      agenda.joinAppointment(Appointment(dateInterval, place))*/
    //case p @ GoOutProposal(dateInterval, place, leader)
    case Stop() =>  this.endSimulation()
    case msg => println(s"Not expected [Person]: $msg")
  }

  private def busy(): Unit = {
    println(this.person)
    Thread.sleep(2)
    coordinator ! Acknowledge()
  }

  private def sendAckIfReady(): Unit = {
    if(waitingResponses.isEmpty) coordinator ! Acknowledge()
  }

  private def goingOutConfirmed(goOutProposal: GoOutProposal, withPerson: Person): Unit = {
    friendsFound = true
    val appointment = Appointment(goOutProposal.dateInterval, goOutProposal.place)
    agenda.fixAppointment(appointment, Multiple(person, Set(person, withPerson)))(_ + withPerson)
    //Logger.info(s"Added fixed appointment $appointment")
  }

  //TODO NextAction :Unit

  //TODO NextCommitment: Unit

  private def comeBack(): Unit = {
    isOut = false
    //TODO fare rientrare nell'abitazione
    person.setMask(None)
    currentCommitment = None
  }

  private def goOut(): Unit = {
    isOut = true
    //TODO fare uscire dall'abitazione
  }

  private def chooseMask(location: Location): Option[Mask] = if(inPandemic /*&& !location.isInstanceOf[Habitation]*/)
    location.mask match {
      case mask @ Some(_) /*if Random.nextDouble() < maskProbability*/ => mask
      case _ if Random.nextDouble() < maskProbability => Some(Masks.Surgical)
      case _ => None
    } else None


  //TODO TryEnterInPlace in mod @tailrec

  //TODO randomOpenPlace

  //TODO organizeGoingOut

  private def updateWantsToGoOutToday(time: Calendar): Unit = {
    friendsFound = false
    wantsToGoOutToday = true //TODO gestire la probabilità in questa riga
  }

  //TODO mayAcceptProposal :Boolean

  private def mayGoOut(): Boolean = wantsToGoOutToday && (!(lockdown || person.isInfected) ||
    Random.nextDouble() < notRespectingIsolation)

  //TODO wantsToGoOut :Boolean

  private def randomFriends: Seq[ActorRef] = Random.shuffle(person.friends)
    .take(RandomGeneration.randomIntFromGaussian(averageGoingOutFriends, 3, 1))
    .map(friends).toSeq

  private def endSimulation():Unit = {
    context.stop(self)
  }
}
