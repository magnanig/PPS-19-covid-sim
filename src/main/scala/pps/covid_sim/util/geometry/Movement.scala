package pps.covid_sim.util.geometry

case class Movement(movement: Coordinates => Coordinates){
  def apply(coordinates: Coordinates): Coordinates = movement(coordinates)
}

object Movement {
  def apply(): Movement = new Movement(identity)
}
