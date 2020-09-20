package pps.covid_sim.util.geometry

case class Dimension(width: Double, length: Double) {
  /**
   * The surface of the current space of dimension width x length
   */
  val surface: Double = width * length
}

object Dimension {
  implicit def doubleTupleToDimension(dimension: (Double, Double)): Dimension = Dimension(dimension._1, dimension._2)

  implicit def intTupleToDimension(dimension: (Int, Int)): Dimension = Dimension(dimension._1, dimension._2)
}
