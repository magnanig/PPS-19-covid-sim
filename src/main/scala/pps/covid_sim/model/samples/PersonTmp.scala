package pps.covid_sim.model.samples

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City

case class PersonTmp(override val birthDate: Calendar,
                     override val residence: City,
                     infected: Boolean = false,
                     recovered: Boolean = false,
                     death: Boolean = false) extends Person {

  override def isRecovered: Boolean = recovered

  override def isInfected: Boolean = infected

  override def isDeath: Boolean = death

}
