package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.creation.WorldCreation.closedPlaceInLockdown
import pps.covid_sim.model.people.People.{Student, Teacher}
import pps.covid_sim.model.places.Education.{Classroom, University}
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.{Course, Place}
import pps.covid_sim.model.samples.Places
import pps.covid_sim.model.scheduling.Planning.{StudentPlan, WorkPlan}
import pps.covid_sim.parameters.CreationParameters
import pps.covid_sim.util.time.Time.{Day, Month}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

private[institute] case class UniversityCreation() {

  def create(city: City,
             students: List[Student],
             teachers: List[Teacher]): List[Place] = {

    var universities: List[University] = List()
    var numStudent: Int = 0
    val totalStudent: Int = students.size
    val studentPerUni = CreationParameters.studentsUniPerClass * CreationParameters.classesPerUni

    (1 to totalStudent)
      .grouped(studentPerUni) // each group identifies a new university
      .foreach(uniGroup => { // scan all students of each university
        val university: University = University(city, Places.UNIVERSITY_TIME_TABLE, !closedPlaceInLockdown.contains(classOf[University]))
        var classes: List[Classroom] = List()
        var lessonId: Int = 0
        uniGroup
          .grouped(CreationParameters.studentsUniPerClass)
          .foreach(classGroup => { // scan all students of each classroom
            // Each class corresponds to a course. Each course is always done in the same class.
            val classroom: Classroom = Classroom(classGroup.size)
            val lesson: Course = Course(lessonId.toString) // psychology, engineering, ecc..
            val studentPlan: StudentPlan = StudentPlan(!closedPlaceInLockdown.contains(classOf[University]))
              .add(classroom, Day.MONDAY -> Day.FRIDAY, 8 -> 17)
              .commit()

            university.addStudentPlan(lesson, studentPlan) // assign the course to the university
            students.slice(numStudent, numStudent + classGroup.size).foreach(student => {
              student.setLesson(university, lesson) // associate the Course for each student
            })

            lessonId += 1
            classes = classroom :: classes
            numStudent += classGroup.size
          })
        university.addRooms(classes) // add the classes created to the university
        universities = university :: universities
      })
    // assign teachers to the different classrooms of all the university in the city
    assignTeachersToSchools(teachers, universities)
    universities
  }

  private def assignTeachersToSchools(teachers: List[Teacher], universities: List[University]): Unit = {
    // I can do this because at the beginning I know that I have created a
    // number of professors equal to 1/3 of the number of students
    val teachersPerUni: Int = (CreationParameters.studentsUniPerClass * CreationParameters.classesPerUni) / 3
    var numTeacher: Int = 0

    for ((teacherGroup, university) <- teachers.grouped(teachersPerUni).toList.zip(universities)) {
      assignTeachersToSchool(teachers.slice(numTeacher, numTeacher + teacherGroup.size), university)
      numTeacher += teacherGroup.size
    }
  }

  private def assignTeachersToSchool(teachers: List[Teacher], university: University): Unit = {
    // From Monday to Saturday are 6 days. There are 5 hours of lessons every day.
    // 5 * 6 * classRooms.size = hours of lessons to be covered by the professors
    val classRooms: List[Classroom] = university.getRooms.toList
    val slotPerProf: Int = Math.round(((5 * 6 * classRooms.size) / teachers.size).toFloat)
    val slots = WorkingTimeSlots(classRooms, daysInterval = Day.MONDAY -> Day.FRIDAY).iterator

    for (teacher <- teachers) {
      val profPlan: WorkPlan[Classroom] = WorkPlan(!closedPlaceInLockdown.contains(classOf[University]), Month.SEPTEMBER -> Month.MAY)
      (1 to slotPerProf).foreach(_ => {
        if (slots.hasNext) {
          val slot = slots.next
          profPlan.add(slot._1, slot._2, slot._3 -> (slot._3 + 1))
        }
      })
      profPlan.commit()
      teacher.setWorkPlace(university)
    }
  }

}
