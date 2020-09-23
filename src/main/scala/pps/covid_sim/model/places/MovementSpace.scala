package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.util.geometry.{Coordinates, Movement}

trait MovementSpace extends DelimitedSpace {

  /**
   * A function that updates the position starting from the current one and velocity.
   */
  protected val movement: Coordinates => Coordinates

  /**
   * The coordinates of the location entrance.
   */
  val entranceCoords: Coordinates

  /**
   * Generate a fresh movement instance to be assigned to people when enter to the
   * current location.
   * @return  a new movement instance, starting from the entrance
   * @note    each person must have his own movement instance, in order to properly move
   */
  def freshMovement: Movement = Movement(movement)

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