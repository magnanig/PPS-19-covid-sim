package pps.covid_sim.controller.actors

import pps.covid_sim.controller.actors.ActorsCoordination.RegionCoordinator
import pps.covid_sim.model.places.Locality.{Province, Region}

object CoordinatorCommunication {

  case class SetRegion(region: Region)

  case class SetProvince(province: Province, upperCoordinator: Coordinator)

}
