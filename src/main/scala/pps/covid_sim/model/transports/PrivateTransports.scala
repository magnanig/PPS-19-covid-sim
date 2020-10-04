package pps.covid_sim.model.transports

/**
 * Different kind of private transports.
 */
object PrivateTransports {

  trait PrivateTransport extends Transport {

  }

  case class Car(override val capacity: Int) extends PrivateTransport {

  }

}
