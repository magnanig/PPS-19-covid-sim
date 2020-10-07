package pps.covid_sim.model.creation

import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.OpenPlaces.{Field, Park, Square}
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.samples.Places
import pps.covid_sim.parameters.CreationParameters

private[creation] case class OpenPlacesCreation() {

  def create(city: City): List[Place] = {
    var openPlaces: List[Place] = List()
    openPlaces = Square(city, !closedPlaceInLockdown.contains(classOf[Square])) :: openPlaces // square creation
    iterateOverResidence(city, Math.max(5, Math.round(1500 * CreationParameters.citizensPercentage).toInt),
      () => { openPlaces = Park(city, !closedPlaceInLockdown.contains(classOf[Park])) :: openPlaces }) // park creation
    iterateOverResidence(city, Math.max(5, Math.round(2500 * CreationParameters.citizensPercentage).toInt),
      () => { openPlaces = Field(city, Places.FOOTBALL_FIELD_PUBLIC_TIME_TABLE,
        !closedPlaceInLockdown.contains(classOf[Field])) :: openPlaces }) //field creation
    openPlaces
  }

  private def iterateOverResidence[A](city: City, rate: Int, function: () => A): Unit = {
    (1 to city.numResidents).grouped(rate).filter(_.size >= rate).foreach(_ => function())
  }

}
