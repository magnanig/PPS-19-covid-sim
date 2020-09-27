package pps.covid_sim.people.actors

import java.util.Calendar

import akka.actor.ActorRef
import pps.covid_sim.controller.actors.ActorsCoordination.RegionCoordinator
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{City, Province, Region}
import pps.covid_sim.util.time.DatesInterval
//import pps.covid_sim.util.time.Planning.Plan

object Communication {

  /**
   * An implicit to use optional parameter in GoOutResponse without explicitly wrap it into an Option.
   *
   * @param askGoOut the askGoOut object to be wrapped
   * @return the askGoOut object wrapped into an Option (Some)
   */
  implicit def askGoOutToOption(askGoOut: GoOutProposal): Option[GoOutProposal] = Some(askGoOut)

  /**
   * A request to another person, in order to ask him/her to go out in a specific place and time.
   *
   * @param dateInterval the date interval at which sender wants to meet other person
   * @param place        the place where sender wants to meet other person
   */
  case class GoOutProposal(dateInterval: DatesInterval, place: Place, leader: Person)

  /**
   * The response associated to a previous request. If the response sender wants/can (to) go out but
   * in a different place and/or time, he (or she) can respond with false and with a new proposal.
   * Otherwise, if he/she doesn't want anyway, he/she will respond with just a false boolean.
   *
   * @param response   the response associated to a previous request. If false, it is possible to
   *                   specify a new proposal. If true, the second optional parameter will be ignored
   * @param request    the request associated to the current response
   * @param newRequest a new optional proposal
   */
  case class GoOutResponse(response: Boolean,
                           request: GoOutProposal,
                           newRequest: Option[GoOutProposal] = None)
                          (implicit val from: Person)

  object GoOutResponse_ {
    // necessary in order to add "from" parameter in pattern matching
    def unapply(arg: GoOutResponse): Option[(Boolean, GoOutProposal, Option[GoOutProposal], Person)] = Some(
      (arg.response, arg.request, arg.newRequest, arg.from)
    )
  }

  /**
   * The clock time, specifying that a new hour has been begun.
   * This message must be sent one and only once every hour to the subordinate actors.
   */
  case class Tick(calendar: Calendar)

  /**
   * This message specify that virus have been propagate to people on transports. And now people is moving to places.
   * This message must be sent one and only once every hour to the subordinate actors, some actors may not using transports.
   */
  case class InnerTick(calendar: Calendar)

  case class ActorsFriendsMap(friends: Map[Person, ActorRef])

  case class SetPerson(person: Person)

  case class SetRegion(region: Region)

  case class SetProvince(province: Province, upperCoordinator: RegionCoordinator)


  /**
   *
   * @param isInfectedKnown whether it is known the current person is infected (true) or not (false)
   * @param isInfected      whether the current person is contagious (true) or not (false)
   * @param isRecovered     whether the current person is recovered (true) or not (false)
   */
  case class HealthState(isInfectedKnown: Boolean, isInfected: Boolean, isRecovered: Boolean)

  case class SecurityMeasures(isWearingMask: Boolean, isKeepingDistance: Boolean)

  //case class AddPlan[T <: Location](plan: Plan[T])

  //case class ReplacePlan[T <: Location](oldPlan: Plan[T], newPlan: Plan[T] = null)

  /**
   * The acknowledge of actors who have ended their task and are waiting for the next Tick.
   */
  case class Acknowledge()

  /**
   * The acknowledge of actors that were in some means and they are waiting for the next InnerTick.
   * Those actors are the ones included in the transports propagate virus.
   * (those who weren't in means send this ack immediately)
   */
  case class InnerAcknowledge()

  /**
   * The message that indicate the end of the simulation and the need to stop all actors.
   * If an actor receive this, it must be propagated to subordinated actors if present, and stop himself
   */
  case class Stop()

  case class GetPlacesByCity(city: Option[City],placeClass: Option[Class[_ <: Place]], datesInterval: Option[DatesInterval])
  case class GetPlacesByProvince(Province: Option[Province],placeClass: Option[Class[_ <: Place]], datesInterval: Option[DatesInterval])
  case class RequestedPlaces(places: List[Place])

  case class Lockdown(enabled: Boolean)

}
