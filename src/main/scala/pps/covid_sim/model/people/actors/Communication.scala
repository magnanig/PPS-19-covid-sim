package pps.covid_sim.model.people.actors

import java.util.Calendar

import akka.actor.ActorRef
import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City}
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.scheduling.Plan
import pps.covid_sim.model.transports.PublicTransports.{Line, PublicTransport}
import pps.covid_sim.util.time.DatesInterval

object Communication {

  /**
   * A request to another person for asking him/her to go out in a specified place and time.
   *
   * @param datesInterval the dates interval at which sender wants to meet other person
   * @param place         the place where sender wants to meet other person
   */
  case class GoOutProposal(datesInterval: DatesInterval, place: Place, leader: Person)

  /**
   * The response associated to a previous request. If the response sender wants to go out
   * but in a different place and/or time, he can respond with false and with a new proposal.
   * Otherwise, if he doesn't want to go out anyway, he has to respond with just a false boolean.
   *
   * @param response    the response associated to a previous request. If false, it is possible to then
   *                    specify a new proposal. If true, the last parameter doesn't have to be set
   * @param request     the request associated to the current response
   * @param newRequest  a new optional proposal
   * @param from        the person sending the response
   */
  case class GoOutResponse(response: Boolean,
                           request: GoOutProposal,
                           newRequest: Option[GoOutProposal],
                           from: Person)
  object GoOutResponse {
    def apply(response: Boolean,
              request: GoOutProposal,
              newRequest: Option[GoOutProposal] = None)
             (implicit from: Person, unused: DummyImplicit): GoOutResponse = new GoOutResponse(response, request,
      newRequest, from)
  }

  /**
   * The clock time, specifying that a new hour has been begun.
   * This message must be sent one and only once every hour.
   */
  case class HourTick(calendar: Calendar)

  /**
   * The map from the current person friends to the corresponding people actors.
   * @param friends   the mapping from person to his actor representation
   */
  case class ActorsFriendsMap(friends: Map[Person, ActorRef])

  /**
   * Sets the person that must be wrapped inside the receiver actor.
   * @param person    the person to be wrapped by receiver actor
   */
  case class SetPerson(person: Person)

  /**
   * Set desired covid infection parameters.
   * @param covidInfectionParameters  the covid infection parameters
   */
  case class SetCovidInfectionParameters(covidInfectionParameters: CovidInfectionParameters)

  /**
   * Adds the specified plan to the receiver actor's plans.
   * @param plan  the plan to be added
   * @tparam T    the type of location used in plan
   */
  case class AddPlan[T <: Location](plan: Plan[T])

  /**
   * Removes the specified plan to the receiver actor's plans.
   * @param plan  the plan to be removed
   * @tparam T    the type of location used in plan
   */
  case class RemovePlan[T <: Location](plan: Plan[T])

  /**
   * Communicates a new lockdown status change.
   * @param enabled   the new lockdown status: true if enabled, false otherwise
   */
  case class Lockdown(enabled: Boolean)

  case class Acknowledge()

  /**
   * The message that indicate the end of the simulation and the need to stop all actors.
   * If an actor receive this, it must be propagated to subordinated actors if present, and stop himself
   */
  case class Stop()

  case class GetPlacesInArea(area: Area, placeClass: Class[_ <: Place], datesInterval: Option[DatesInterval] = None)

  case class RequestedPlaces(places: List[Place])

  case class GetBusLines(from: City, time: Calendar)

  case class GetTrainLines(from: City, time: Calendar)

  case class RequestedLines(lines: List[Line[PublicTransport]])

  /**
   * An implicit to use optional parameter in GoOutResponse without explicitly wrap it into an Option.
   *
   * @param askGoOut the askGoOut object to be wrapped
   * @return the askGoOut object wrapped into an Option (Some)
   */
  implicit def askGoOutToOption(askGoOut: GoOutProposal): Option[GoOutProposal] = Some(askGoOut)

}
