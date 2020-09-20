package pps.covid_sim.util.geometry

trait GeometryEntity {

  /**
   * Get the distance between this and the specified point
   * @param point   the other point
   * @return        the distance between this and point
   */
  def -(point: Coordinates): Double

}
