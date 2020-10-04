package pps.covid_sim.util.geometry

/**
 * Utility class modeling possible speeds of moving people.
 * @param delta   the adjustment factor with respect to a base delta, depending on the chosen speed
 */
case class Speed private(delta: Double)

object Speed {
  private[Speed] def apply(delta: Double): Speed = new Speed(delta)

  val SLOW: Speed = Speed(1.0)
  val MIDDLE: Speed = Speed(1.5)
  val FAST: Speed = Speed(2.0)
}
