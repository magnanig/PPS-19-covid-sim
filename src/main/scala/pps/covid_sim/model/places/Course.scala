package pps.covid_sim.model.places

import pps.covid_sim.model.places.Education.Lesson

case class Course(name: String) extends Lesson

object Course {

  val PSICOLOGIA: Course = Course("Psicologia")
  val INGEGNERIA_CIVILE: Course = Course("Ingegneria Civile")
  val INGEGNERIA_BIOMEDICA: Course = Course("Ingegneria Biomedica")
  val INGEGNERIA_ELETTRONICA: Course = Course("Ingegneria Elettronica")
  val INGEGNERIA_AEROSPAZIALE: Course = Course("Ingegneria Aerospaziale")
  val INGEGNERIA_E_SCIENZE_INFORMATICHE: Course = Course("Ingegneria e Scienze Informatiche")

}