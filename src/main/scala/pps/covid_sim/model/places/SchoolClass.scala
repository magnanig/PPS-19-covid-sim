package pps.covid_sim.model.places

import pps.covid_sim.model.places.Education.Lesson

case class SchoolClass(name: String) extends Lesson

object SchoolClass {

  val PRIMA_A: SchoolClass = SchoolClass("1A")
  val SECONDA_A: SchoolClass = SchoolClass("2A")

}