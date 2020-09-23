package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.util.geometry.Coordinates

trait MovementSpace extends DelimitedSpace {

  /**
   * A function that updates the position starting from the current one and velocity.
   */
  protected val movement: (Coordinates, Set[Person]) => Coordinates

  /**
   * Move all people according to the current place movement function.
   */
  def move(): Unit = currentGroups.flatten
    .foreach(person => person.position = movement(person.position, currentGroups.flatten - person))

  /**
   * The coordinates of the location entrance.
   */
  val entranceCoords: Coordinates

  override protected def onEntered(group: Group): Unit = {
    group.foreach(_.position = Coordinates.random(dimension)) // TODO
  }

  override def propagateVirus(time: Calendar, place: Place): Unit = {
    super.propagateVirus(time, place)
    currentGroups.flatMap(_.people).toList.combinations(2)
      .map(pair => (pair, pair.head.position - pair.last.position))
      .foreach(e => VirusPropagation.tryInfect(e._1.head, e._1.last, place, time)(e._2))
  }

}