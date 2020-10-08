package pps.covid_sim.model.places

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.people.People.{Student, Teacher}
import pps.covid_sim.model.places.Education.{Classroom, School}
import pps.covid_sim.model.samples.Cities
import pps.covid_sim.util.scheduling.Planning.{StudentPlan, WorkPlan}
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, ScalaCalendar}
import pps.covid_sim.util.time.TimeIntervalsImplicits._

class ClassroomTest {

  val prof1: Teacher = Teacher(ScalaCalendar(1997, 1, 26), Cities.CERVIA)
  val prof2: Teacher = Teacher(ScalaCalendar(1997, 5, 12), Cities.CERVIA)

  val mario: Student = Student(ScalaCalendar(1997, 1, 26), Cities.CERVIA)
  val luigi: Student = Student(ScalaCalendar(1997, 5, 12), Cities.CERVIA)

  val classroom46: Classroom = Classroom(25)
  val lab32: Classroom = Classroom(35)

  val studentPlan: StudentPlan = StudentPlan(enabledInLockdown = false)
    .add(classroom46, Day.MONDAY -> Day.TUESDAY,  8 -> 13)
    .add(lab32, Day.WEDNESDAY,8 -> 12)
    .add(classroom46, Day.THURSDAY -> Day.FRIDAY, 8 -> 13)
    .dayPlan(Day.SATURDAY)
      .add(classroom46, 8 -> 10)
      .add(lab32, 10 -> 12)
      .add(classroom46, 12 -> 13)
      .commit()

  val prof1Plan: WorkPlan[Classroom] = WorkPlan(enabledInLockdown = false)
    .add(classroom46, Day.MONDAY -> Day.TUESDAY,  8 -> 10)
    .add(lab32, Day.WEDNESDAY,10 -> 11)
    .add(classroom46, Day.THURSDAY -> Day.FRIDAY, 9 -> 10)
    .dayPlan(Day.SATURDAY)
    .add(classroom46, 8 -> 10)
    .add(lab32, 11 -> 12)
    .add(classroom46, 12 -> 13)
    .commit()

  //val prof2Plan = null // ...

  val itis: School = School(
    Cities.CERVIA,
    TimeTable()   // when school is open
      .add(Day.MONDAY -> Day.FRIDAY, 8 -> 16)
      .add(Day.SATURDAY, 8 -> 14),
    openedInLockdown = false
  )

  itis.addWorkPlan(prof1, prof1Plan)

  prof1.setWorkPlace(itis)
  prof2.setWorkPlace(itis)

  mario.setLesson(itis, null)
  luigi.setLesson(itis, null)

  @Test
  def testSingleEntering(): Unit = {
    val time = ScalaCalendar(2020, 8, 4, 11) // Tue, h 11.00
    assertEquals(0, lab32.numCurrentPeople)
    assert(lab32.enter(mario, time).isDefined)
    assertEquals(1, lab32.numCurrentPeople)

    assert(lab32.getStudentDesk(mario).exists(!_.isFree))
    assertEquals(1, lab32.numCurrentPeople)

    lab32.exit(mario)
    assert(!lab32.getStudentDesk(mario).exists(_.isFree))
    assertEquals(0, lab32.numCurrentPeople)
  }

  @Test
  def testEnteringFullClassroom(): Unit = {
    val time = ScalaCalendar(2020, 8, 4, 11) // Tue, h 11.00
    val students = (1 to classroom46.capacity).map(_ => Student(ScalaCalendar(1997, 1, 1), Cities.CESENA))
    assert(students.forall(classroom46.enter(_, time).isDefined))
    assert(students.forall(classroom46.getStudentDesk(_).isDefined)) // be sure each student has his desk assigned
    assert(classroom46.enter(mario, time).isEmpty) // full classroom -> students can't enter anymore
    assert(classroom46.enter(prof1, time).isDefined) // teacher can enter anyway
  }

}
