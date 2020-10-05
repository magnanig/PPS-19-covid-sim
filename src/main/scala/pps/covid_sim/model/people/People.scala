package pps.covid_sim.model.people

import java.util.Calendar

import pps.covid_sim.model.places.Education.{Institute, Lesson}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.WorkPlace

object People {

  trait Employed extends Person {
    private var _workPlace: WorkPlace[_ <: Location] = _

    def setWorkPlace(workPlace: WorkPlace[_ <: Location]): Unit = {
      _workPlace = workPlace
    }

    def workPlace: WorkPlace[_ <: Location] = _workPlace
  }

  case class Student(override val birthDate: Calendar, override val residence: City) extends Person {
    private var _institute: Institute = _
    private var _lesson: Lesson = _

    def institute: Institute = _institute

    def lesson: Lesson = _lesson

    def setLesson(institute: Institute, lesson: Lesson): Unit = {
      _institute = institute
      _lesson = lesson
    }

    override def toString: String = "Student"
  }

  case class Worker(override val birthDate: Calendar, override val residence: City) extends Employed {
    override def toString: String = "Worker"
  }

  case class Teacher(override val birthDate: Calendar, override val residence: City) extends Employed {
    override def toString: String = "Teacher"
  }

  case class Unemployed(override val birthDate: Calendar, override val residence: City) extends Person {
    override def toString: String = "Unemployed"
  }

}
