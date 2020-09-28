package pps.covid_sim.controller.actors

import akka.actor.ActorRef
import pps.covid_sim.controller.actors.ActorsCoordination.system

import scala.collection.parallel.ParSet

trait Coordinator {

  //private[actors] var system: ActorSystem = _



  private[actors] var _subordinatedActors: ParSet[ActorRef] = ParSet()

  def subordinatedActors: ParSet[ActorRef] = _subordinatedActors

  private[actors] var currentInfections: Int = 0

  private[actors] var waitingAck: ParSet[ActorRef] = ParSet()

  private[controller] def close(): Unit = synchronized {
    system.terminate()
  }


}
