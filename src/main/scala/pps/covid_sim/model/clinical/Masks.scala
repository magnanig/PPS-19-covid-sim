package pps.covid_sim.model.clinical

// values below from
// https://www.corriere.it/dataroom-milena-gabanelli/mascherine-come-sono-fatte-che-cosa-servono-cosa-filtrano-come-riutilizzarle/e7db0f72-78f1-11ea-ab65-4f14b5300fbb-va.shtml)

object Masks {

  sealed abstract class Mask private[Masks](val incomingFiltering: Double, val outgoingFiltering: Double)

  object Surgical extends Mask(0.2, 0.95)

  object FFP1WithoutValve extends Mask(0.72, 0.72)

  object FFP2WithoutValve extends Mask(0.92, 0.92)

  object FFP3WithoutValve extends Mask(0.98, 0.98)

  object FFP1WithValve extends Mask(0.72, 0.2)

  object FFP2WithValve extends Mask(0.92, 0.2)

  object FFP3WithValve extends Mask(0.98, 0.2)

}
