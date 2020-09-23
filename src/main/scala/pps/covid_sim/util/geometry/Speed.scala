package pps.covid_sim.util.geometry

case class Speed private(delta: Double)

object Speed {
  private[Speed] def apply(delta: Double): Speed = new Speed(delta)

  val SLOW: Speed = Speed(0.01)
  val MIDDLE: Speed = Speed(0.05)
  val FAST: Speed = Speed(0.1)
}
