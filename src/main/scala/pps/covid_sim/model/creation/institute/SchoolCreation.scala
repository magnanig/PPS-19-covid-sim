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

  def create(city: City,
             students: List[Student],
             teachers: List[Teacher]): List[Place] = {

    var schools: List[School] = List()
    var numStudent: Int = 0
    val totalStudent: Int = students.size
    val studentPerSchool = CreationParameters.studentsPerClass * CreationParameters.classesPerSchool

    (1 to totalStudent)
      .grouped(studentPerSchool) // each group identifies a new school
      .foreach(schoolGroup => { // scan all students of each school
        val school: School = School(city, Places.SCHOOL_TIME_TABLE)
        var classes: List[Classroom] = List()
        var lessonId: Int = 0
        schoolGroup
          .grouped(CreationParameters.studentsPerClass)
          .foreach(classGroup => { // scan all students of each classroom
            // Each class corresponds to one section. The students of each section always teach in the same room
            val classroom: Classroom = Classroom(classGroup.size)
            val lesson: SchoolClass = SchoolClass(lessonId.toString) // 1A, 2A, 1B, 2B, ecc...
            val studentPlan: StudentPlan = StudentPlan()
              .add(classroom, Day.MONDAY -> Day.SATURDAY, 8 -> 13)
              .commit()

            school.addStudentPlan(lesson, studentPlan) // assign the course to the school
            students.slice(numStudent, numStudent + classGroup.size).foreach(student => {
              student.setLesson(school, lesson) // associate the StudentPlan for each student
            })

            lessonId += 1
            classes = classroom :: classes
            numStudent += classGroup.size
          })
        school.addRooms(classes) // add the classes created to the school
        schools = school :: schools
      })
    // assign teachers to the different classrooms of all the schools in the city
    assignTeachersToSchools(teachers, schools)
    schools
  }

  private def assignTeachersToSchools(teachers: List[Teacher], schools: List[School]): Unit = {
    // I can do this because at the beginning I know that I have created a
    // number of professors equal to 1/3 of the number of students
    val teachersPerSchool: Int = (CreationParameters.studentsPerClass * CreationParameters.classesPerSchool) / 3
    var numTeacher: Int = 0

    for ((teacherGroup, school) <- teachers.grouped(teachersPerSchool).toList.zip(schools)) {
      assignTeachersToSchool(teachers.slice(numTeacher, numTeacher + teacherGroup.size), school)
      numTeacher += teacherGroup.size
    }
  }

  private def assignTeachersToSchool(teachers: List[Teacher], school: School): Unit = {
    // From Monday to Saturday are 6 days. There are 5 hours of lessons every day.
    // 5 * 6 * classRooms.size = hours of lessons to be covered by the professors
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