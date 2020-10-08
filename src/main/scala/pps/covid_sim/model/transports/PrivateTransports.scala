package pps.covid_sim.model.transports
import pps.covid_sim.model.clinical.Masks

/**
 * Different kind of private transports.
 */
object PrivateTransports {

  trait PrivateTransport extends Transport {

  }

  case class Car(override val capacity: Int) extends PrivateTransport {
    override def mask: Option[Masks.Mask] = None
  }

}
