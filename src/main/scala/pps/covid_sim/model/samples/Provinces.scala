package pps.covid_sim.model.samples

import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}

object Provinces {

  val AOSTA: Province = Province(7003, "Aosta", "AO", Regions.VALLE_DAOSTA, 45.73750285, 7.32014937)
  val RIMINI: Province = Province(99014, "Rimini", "RN", Regions.EMILIA_ROMAGNA, 44.06090086, 12.56562951)
  val RAVENNA: Province = Province(39014, "Ravenna", "RA", Regions.EMILIA_ROMAGNA, 44.41722492, 12.19913937)
  val FORLI_CESENA: Province = Province(40012, "Forlì", "FC", Regions.EMILIA_ROMAGNA, 44.22268559, 12.04068608)
  val BOLOGNA: Locality.Province = Province(37006,"Bologna", "BO", Region.EMILIA_ROMAGNA, 44.49436680, 11.34172080)
  val MILANO: Locality.Province = Province(15146,"Milano", "MI", Region.LOMBARDIA, 45.46679408, 9.19034740)
  val TORINO: Locality.Province = Province(1272,"Torino", "TO", Region.PIEMONTE, 45.07327450, 7.68068748)
  val ROMA: Locality.Province = Province(58091,"Roma", "RO", Region.LAZIO, 41.89277044, 12.48366723)

}
