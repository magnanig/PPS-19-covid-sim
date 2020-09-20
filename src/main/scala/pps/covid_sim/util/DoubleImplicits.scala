package pps.covid_sim.util

object DoubleImplicits {

  implicit class AlmostEqualsDouble(d: Double) {
    private val precision: Double = 0.001
    def ~=(d2: Double): Boolean = (d - d2).abs <= precision

    def ~<=(d2: Double): Boolean = (d2 - d) >= -precision
  }

  implicit class RichDouble(d: Double) {
    def ^ (exp: Double): Double = Math.pow(d, exp)
  }

}
