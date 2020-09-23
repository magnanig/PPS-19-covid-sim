package pps.covid_sim.model.people

import java.util.Calendar

import pps.covid_sim.model.places.Locality.City

object People {

  trait Employed extends Person

  case class Unemployed(override val birthDate: Calendar, override val residence: City) extends Person {
    override def toString: String = "Unemployed"
  }


  case class Student(override val birthDate: Calendar, override val residence: City) extends Person


  case class Worker(override val birthDate: Calendar, override val residence: City) extends Employed {
    override def toString: String = "Worker"
  }


  case class Teacher(override val birthDate: Calendar, override val residence: City) extends Employed {
    override def toString: String = "Teacher"
  }

}
