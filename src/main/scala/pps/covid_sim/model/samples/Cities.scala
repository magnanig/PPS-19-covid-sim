package pps.covid_sim.model.samples

import pps.covid_sim.model.places.Locality.City

/**
 * Some cities to be used in tests
 */
object Cities {

  // è sia una città che provincia, infatti l'ID coincide con Provinces.RAVENNA
  val RAVENNA: City = City(39014, "Ravenna", 154288, Provinces.RAVENNA, 44.41722492, 12.19913937)
  val CERVIA: City = City(39007, "Cervia", 28886, Provinces.RAVENNA, 44.26092823, 12.34981530)
  val LUGO : City = City(39012, "Lugo", 32016, Provinces.RAVENNA, 44.42083388, 11.91148250)
  val FAENZA: City = City(39010, "Faenza", 57973, Provinces.RAVENNA, 11.91148250, 11.88279721)
  val FORLI: City = City(40012, "Forlì", 116029, Provinces.FORLI_CESENA, 44.22268559, 12.04068608)
  val CESENA: City = City(40007, "Cesena", 96984, Provinces.FORLI_CESENA, 44.13654899, 12.24217492)
  val CESENATICO: City = City(40008, "Cesenatico", 25686, Provinces.FORLI_CESENA, 44.19953513, 12.39544168)
  val GAMBETTOLA: City = City(40015, "Gambettola", 10421, Provinces.FORLI_CESENA, 44.12000641, 12.33907227)
  val RIMINI: City = City(99014, "Rimini", 143731, Provinces.RIMINI, 44.06090086, 12.56562951)
  val RICCIONE: City = City(99013, "Riccione", 34323, Provinces.RIMINI, 44.00002553, 12.65505776)
  val CATTOLICA: City = City(99002, "Cattolica" , 16590, Provinces.RIMINI, 37.43952809, 13.39539987)
  val AOSTA: City = City(7003, "Aosta", 34657, Provinces.AOSTA, 45.73750285, 7.32014937)

}
