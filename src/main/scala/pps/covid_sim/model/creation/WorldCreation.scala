package pps.covid_sim.model.creation

import pps.covid_sim.model.container.PeopleContainer
import pps.covid_sim.model.places.Locality.Region

import scala.collection.mutable

//TODO  Questo oggetto, volendo (se dovesse servire o fare comodo)
// potrebbe contenere una mappa che, per ogni regione,contiene
// tutti i posti contenuti in quella specifica regione,
// mantenendo salvato il riferimeto di ogni RegionPlacesCreation.
// In questo modo, potranno essere effettuate delle ricerche
// per ogni Regione, deve "lanciare" un'istanza di
// mirate per ogni regione in modo rapido ed efficiente.

object WorldCreation {

  private var regions: mutable.Map[Int, Region] = mutable.Map[Int, Region]() // id_region -> Region

  // Per ogni Regione viene lanciata un'istanza di RegionPlacesCreation.
  // In questo modo, per ogni regione, vengono create città, persone e
  // luoghi comuni di aggregazione.
  def generateAll(): Unit = {
    regionsCreation()
    regions.values.foreach(RegionPlacesCreation.create)
    PeopleContainer.checkAssignedWork()
  }

  private def regionsCreation(): Unit = {
    val bufferedSource = io.Source.fromFile("res/italy_regions.csv")
    for (line <- bufferedSource.getLines) {
      val Array(id_region, name, _, num_residents, _, _) = line.split(";")
      regions += (id_region.toInt -> Region(id_region.toInt, name, num_residents.toInt))
    }
    bufferedSource.close
  }

}
