package pps.covid_sim.controller.actors

import pps.covid_sim.model.places.Locality.{Area, Province, Region}
import pps.covid_sim.util.time.DatesInterval

object CoordinatorCommunication {

  case class SetRegion(region: Region)

  case class SetProvince(province: Province)

  case class Init(area: Area, datesInterval: DatesInterval)

}
