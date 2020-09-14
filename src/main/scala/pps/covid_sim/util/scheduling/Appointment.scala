package pps.covid_sim.util.scheduling

import pps.covid_sim.model.places.Place
import pps.covid_sim.util.time.DatesInterval

case class Appointment(datesInterval: DatesInterval,
                       place: Place) extends Ordered[Appointment] {
  override def compare(that: Appointment): Int = datesInterval.from.compareTo(that.datesInterval.from)

  override def toString: String = s"Appointment($datesInterval in ${place.getClass.getSimpleName})"

}
