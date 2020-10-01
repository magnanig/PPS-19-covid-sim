package pps.covid_sim.model.samples

import pps.covid_sim.model.places.Locality.City

/**
 * Some cities to be used in tests
 */
object Cities {

  val AOSTA: City = City(7003, "Aosta", 34657, Provinces.AOSTA)
  val RAVENNA: City = City(39014, "Ravenna", 154288, Provinces.RAVENNA) // è sia una città che provincia, infatti l'ID coincide con Provinces.RAVENNA
  val CERVIA: City = City(39007, "Cervia", 28886, Provinces.RAVENNA)
  val LUGO : City = City(39012, "Lugo", 32016, Provinces.RAVENNA)
  val FAENZA: City = City(39010, "Faenza", 57973, Provinces.RAVENNA)
  val FORLI: City = City(40012, "Forlì", 116029, Provinces.FORLI_CESENA)
  val CESENA: City = City(40007, "Cesena", 96984, Provinces.FORLI_CESENA)
  val CESENATICO: City = City(40008, "Cesenatico", 25686, Provinces.FORLI_CESENA)
  val GAMBETTOLA: City = City(40015, "Gambettola", 10421, Provinces.FORLI_CESENA)
  val RIMINI: City = City(99014, "Rimini", 143731, Provinces.RIMINI)
  val RICCIONE: City = City(99013, "Riccione", 34323, Provinces.RIMINI)
  val CATTOLICA: City = City(99002, "Cattolica" , 16590, Provinces.RIMINI)

}
