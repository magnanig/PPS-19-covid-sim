package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.util.geometry.Coordinates

trait MovementSpace extends DelimitedSpace {

  /**
   * A function that generates a sampling of the path of a person inside the place,
   * starting from the current position and velocity.
   */
  protected val pathSampling: Coordinates => Set[Seq[Map[Person, Seq[Int]]]]

  /**
   * Move all people according to the current place movement function.
   */
  //def move(): Unit = currentGroups.flatten
  //  .foreach(person => person.position = movement(person.position, currentGroups.flatten - person))

  /**
   * The coordinates of the location entrance.
   */
  val entranceCoords: Coordinates

  /**
   * The coordinates of the person before starting the movement.
   * They are randomly generated on the edges to prevent all people
   * starting from the same point at the same time or starting over an obstacle.
   */
  val initialCoords: Coordinates = Coordinates.randomOnBorder(dimension)

  override protected def onEntered(group: Group): Unit = {
    group.foreach(_.position = Coordinates.random(spaceDimension)) // TODO
  }


  /**
   * Method that manages the spread of the virus in different places (both indoors and outdoors),
   * taking into account the movements of people.
   * @param time
   * @param place the place where the person is
   */
  override def propagateVirus(time: Calendar, place: Place): Unit = {
    if (currentGroups.exists(group => group.people.exists(person => person.canInfect))) {
      val sampling = pathSampling(initialCoords) // mi resituisce il mio tipo  Set(List(Map(persona -> [...])))
      // se la distanza è inferiore alla soglia, faccio la tryInfect() tra le persone della stessa lista
      sampling.foreach(timeSlot => timeSlot.foreach(person => checkForNotRespectedDistance))
    }
    // pensare se far muovere i gruppi come se fossero una cosa sola, quindi uso il leader poi la nella tryInfect coinvolgo tutti
  }

  /*
  override def propagateVirus(time: Calendar, place: Place): Unit = {
    super.propagateVirus(time, place)
    currentGroups.flatMap(_.people).toList.combinations(2)
      .map(pair => (pair, pair.head.position - pair.last.position))
      .foreach(e => VirusPropagation.tryInfect(e._1.head, e._1.last, place, time)(e._2))
  }
   */

}