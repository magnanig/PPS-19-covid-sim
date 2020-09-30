package pps.covid_sim.model.samples

import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}

object Provinces {

  val AOSTA: Province = Province(7003, "Aosta", "AO", Regions.VALLE_DAOSTA)
  val RIMINI: Province = Province(99014, "Rimini", "RN", Regions.EMILIA_ROMAGNA)
  val RAVENNA: Province = Province(39014, "Ravenna", "RA", Regions.EMILIA_ROMAGNA)
  val FORLI_CESENA: Province = Province(40012, "Forlì", "FC", Regions.EMILIA_ROMAGNA)
  val BOLOGNA: Locality.Province = Province(37006,"Bologna", "BO", Region.EMILIA_ROMAGNA)
  val MILANO: Locality.Province = Province(15146,"Milano", "MI", Region.LOMBARDIA)
  val TORINO: Locality.Province = Province(1272,"Torino", "TO", Region.PIEMONTE)
  val ROMA: Locality.Province = Province(58091,"Roma", "RO", Region.LAZIO)

}
