package pps.covid_sim.controller.actors.coordinators

import akka.actor.{Actor, ActorRef}
import pps.covid_sim.controller.actors.coordinators.ActorsCoordination.system

import scala.collection.parallel.immutable.ParSet

trait Coordinator extends Actor {

  //private[actors] var system: ActorSystem = _

  private[actors] var _subordinatedActors: ParSet[ActorRef] = ParSet()

  def subordinatedActors: ParSet[ActorRef] = _subordinatedActors

  private[actors] var currentInfections: Int = 0

  private[actors] var waitingAck: ParSet[ActorRef] = ParSet()

  private[controller] def close(): Unit = synchronized {
    system.terminate()
  }


}
