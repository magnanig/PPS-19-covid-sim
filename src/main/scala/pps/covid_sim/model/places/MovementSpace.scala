package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.geometry.Coordinates

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait MovementSpace extends DelimitedSpace {

  /**
   * A function that generates a sampling of the path of a person inside the place,
   * starting from the current position and velocity.
   */
  protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]]

  /**
   * The coordinates of the person before starting the movement.
   * They are randomly generated on the edges to prevent all people
   * starting from the same point at the same time or starting over an obstacle.
   */
  override protected def onEntered(group: Group): Unit = {
    val initialPosition = Coordinates.randomOnBorder(dimension)
    group.foreach(_.position = initialPosition)
  }

  /**
   * Method that manages the spread of the virus in different places (both indoors and outdoors),
   * taking into account the movements of people.
   * @param time  the time when the infection could occur
   * @param place the place where the person is
   */
  override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
    super.propagateVirus(time, place)(covidInfectionParameters)
    // TODO: capire come mai nessuno entra....
    //println(s"!!!!!!!!!! $currentGroups !!!!!!!!!!!!")
    //if (currentGroups.flatten.exists(_.canInfect)) {
      // People from the same group will follow the same path
      val sampling = pathSampling(currentGroups)
      // Assigns the last coordinate of the sampling to the people
      sampling.foreach(timeSlot => timeSlot.foreach(map => map.foreach(path => path._1.people
        .foreach(person => person.position = path._2.last))))
      // If two people inside the same time slot have not kept the safety distance, the contagion attempt occurs
      sampling.foreach(timeSlot => timeSlot.foreach(map =>
        // Attempt to avoid subsequent computations if there are no infected people within the same time slot
        if (map.keys.exists(group => group.people.exists(person => person.canInfect)))
          map.foreach(group => group._1.people.toList.combinations(2).foreach(pair =>
            if (checkForNotMaintainingSafetyDistance(pair.head.position, pair.last.position))
              VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time)))))
    //}
  }

  /**
   *
   * @param coord1 the coordinates of the first person of the pair
   * @param coord2 the coordinates of the second person of the pair
   * @return       true if the two people did not keep the safety distance, false otherwise
   */
  private def checkForNotMaintainingSafetyDistance(coord1: Coordinates, coord2: Coordinates): Boolean = {
    coord1 - coord2 <= 1.0
  }

}