package pps.covid_sim.model.people.actors

object Request extends Enumeration {
  type Request = Value
  val LOOKING_FOR_PLACES, LOOKING_FOR_ALTERNATIVE, LOOKING_FOR_MARKET = Value
}
