package pps.covid_sim.controller.actors.coordinators

import java.util.Calendar

import akka.actor.{ActorRef, ActorSystem, Props}
import pps.covid_sim.controller.Controller
import pps.covid_sim.controller.actors.CoordinatorCommunication.Init
import pps.covid_sim.model.places.Locality.Area
import pps.covid_sim.util.time.DatesInterval

object ActorsCoordination {


  private[coordinators] var system: ActorSystem = _
  private[coordinators] var actorsCoordinator: ActorRef = _

  private[coordinators] var controller: Controller = _
  private[coordinators] var simulationInterval: DatesInterval = _
  private[coordinators] var currentTime: Calendar = _
  private[coordinators] var simulationArea: Area = _

  /**
   * method that allow to create all the hierarchy of coordinators
   *
   * @param controller    that coordinators call to get useful data and run methods
   * @param datesInterval that indicates the interval the user had insert for the simulation
   */
  private[controller] def create(area: Area, controller: Controller, datesInterval: DatesInterval): Unit = {
    println("Going to create actors")
    this.controller = controller
    system = ActorSystem.create()
    actorsCoordinator = system.actorOf(Props[ActorsCoordinator])
    simulationArea = area
    actorsCoordinator ! Init(area, datesInterval)
  }
}
