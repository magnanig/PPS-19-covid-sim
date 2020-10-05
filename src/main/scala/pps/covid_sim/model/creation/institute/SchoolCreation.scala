package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.people.People.{Student, Teacher}
import pps.covid_sim.model.places.Education.{Classroom, School}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.{Place, SchoolClass}
import pps.covid_sim.model.samples.Places
import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.util.scheduling.Planning.{StudentPlan, WorkPlan}
import pps.covid_sim.util.time.Time.{Day, Month}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

private[institute] case class SchoolCreation() {

  /*
   * Lista di studenti composta almeno di 25 x 2 studenti (2 aule)
   */
  def create(city: City,
             students: List[Student],
             teachers: List[Teacher]): List[Place] = {

    var schools: List[School] = List()
    var numStudent: Int = 0
    val totalStudent: Int = students.size
    val studentPerSchool = CreationParameters.studentsPerClass * CreationParameters.classesPerSchool

    // schools number
    (1 to totalStudent)
      .grouped(studentPerSchool)
      .foreach(schoolGroup => { // scorro tutti gli STUDENTI DI OGNI SCUOLA
        val school: School = School(city, Places.SCHOOL_TIME_TABLE)
        var classes: List[Classroom] = List()
        var lessonId: Int = 0
        schoolGroup
          .grouped(CreationParameters.studentsPerClass)
          .foreach(classGroup => { // scorro tutti gli STUDENTI DI OGNI CLASSE
            // Ogni classe corrisponde ad un corso. Ogni corso è tenuto sempre nella medesima classe.
            // Assegnare il corso alla scuola;  Assegnare il corso ad ogni alunno
            val classroom: Classroom = Classroom(classGroup.size)
            val lesson: SchoolClass = SchoolClass(lessonId.toString) // 1A, 2A, 1B, 2B, ecc...
            val studentPlan: StudentPlan = StudentPlan()
              .add(classroom, Day.MONDAY -> Day.SATURDAY, 8 -> 13)
              .commit()

            school.addStudentPlan(lesson, studentPlan) // Associo il "corso" alla scuola
            students.slice(numStudent, numStudent + classGroup.size).foreach(student => {
              student.setLesson(school, lesson) // Associo lo StudentPlan per ogni studente
            })

            lessonId += 1
            classes = classroom :: classes
            numStudent += classGroup.size
          })
        school.addRooms(classes) // Aggiungo le classi create alla scuola
        schools = school :: schools
      })
    // Assegnare i vari professori alle diverse aule di tutte le scuole della città
    assignTeachersToSchools(teachers, schools)
    schools
  }

  private def assignTeachersToSchools(teachers: List[Teacher], schools: List[School]): Unit = {
    // Faccio così perché all'inizio so che ho creato un numero di professori
    // pari ad 1/3 del numero degli studenti
    val teachersPerSchool: Int = (CreationParameters.studentsPerClass * CreationParameters.classesPerSchool) / 3
    var numTeacher: Int = 0

    for ((teacherGroup, school) <- teachers.grouped(teachersPerSchool).toList.zip(schools)) {
      assignTeachersToSchool(teachers.slice(numTeacher, numTeacher + teacherGroup.size), school)
      numTeacher += teacherGroup.size
    }
  }

  private def assignTeachersToSchool(teachers: List[Teacher], school: School): Unit = {
    val classRooms: List[Classroom] = school.getRooms.toList
    val slotPerProf: Int = Math.round(((5 * 6 * classRooms.size) / teachers.size).toFloat)
    val slots = WorkingTimeSlots(classRooms).iterator

    for (teacher <- teachers) {
      val profPlan: WorkPlan[Classroom] = WorkPlan(Month.SEPTEMBER -> Month.MAY)
      (1 to slotPerProf).foreach(_ => {
        if (slots.hasNext) {
          val slot = slots.next
          profPlan.add(slot._1, slot._2, slot._3 -> (slot._3 + 1))
        }
      })
      profPlan.commit()
      teacher.setWorkPlace(school)
    }
  }

}