package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.people.People.{Student, Teacher}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Place
import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.util.Statistic

case class InstitutePlacesCreation() {

  def create(city: City,
             teachers: List[Teacher],
             students: List[Student]): List[Place] = {

    var educationPlaces: List[Place] = List()

    val numStudents: List[Int] = Statistic.totalPercentageToInt(students.size,
      CreationParameters.uniStudentsPercentage, CreationParameters.schoolStudentsPercentage)
    val numTeachers: List[Int] = Statistic.totalPercentageToInt(teachers.size,
      CreationParameters.uniTeachersPercentage, CreationParameters.schoolTeachersPercentage)
    var idS: Int = 0
    var idT: Int = 0

    if (city.isProvince) {
      // Crea università
      educationPlaces = educationPlaces ::: UniversityCreation().create(city,
        students.slice(idS, idS + numStudents.head),
        teachers.slice(idT, idT + numTeachers.head))
      idS += numStudents.head
      idT += numTeachers.head
    }

    if (city.numResidents >= CreationParameters.minResidencesToSchool) {
      educationPlaces = educationPlaces ::: SchoolCreation().create(city,
        students.slice(idS, idS + numStudents.last),
        teachers.slice(idT, idT + numTeachers.last))
    }

    educationPlaces
  }

}
