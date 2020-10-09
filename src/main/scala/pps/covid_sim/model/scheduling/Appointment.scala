package pps.covid_sim.model.scheduling

import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

/**
 * Represents an appointment at the specified dates interval and place.
 * @param datesInterval   the desired dates interval
 * @param place           the place at which appointment refers to
 */
case class Appointment(datesInterval: DatesInterval,
                       place: Place) extends Ordered[Appointment] {
  override def compare(that: Appointment): Int = datesInterval.from.compareTo(that.datesInterval.from)

  override def toString: String = s"Appointment($datesInterval in ${place.getClass.getSimpleName})"

}
