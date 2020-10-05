package pps.covid_sim.model.transports
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.City

/**
 * Different kind of private transports.
 */
object PrivateTransports {

  trait PrivateTransport extends Transport {

  }

  case class Car(override val capacity: Int, override val city: City) extends PrivateTransport {
  }

}
