package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.clinical.{Masks, VirusPropagation}
import pps.covid_sim.model.people.People.{Student, Teacher}
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.arranging.Arrangeable
import pps.covid_sim.model.places.arranging.SchoolDesks.{Desk, DeskGroup, DesksArrangement}
import pps.covid_sim.model.places.rooms.{MultiRoom, Room}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.{Dimension, Rectangle}
import pps.covid_sim.util.scheduling.Planning.StudentPlan
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.ScalaCalendar

object Education {

  trait Lesson

  abstract class Institute(override val city: City,
                           override val openedInLockdown: Boolean,
                           private val classrooms: Seq[Classroom] = Seq())
    extends MultiRoom[Classroom](city, classrooms) with ClosedWorkPlace[Classroom] with LimitedHourAccess {

    private var _studentsPlans: Map[Lesson, StudentPlan] = Map()

    override val mask: Option[Mask] = Some(Masks.Surgical)

    def addStudentPlan(lesson: Lesson, studentPlan: StudentPlan): Unit = { _studentsPlans += lesson -> studentPlan }

    def getStudentPlan(lesson: Lesson): Option[StudentPlan] = _studentsPlans.get(lesson)

    def studentsPlans: Map[Lesson, StudentPlan] = _studentsPlans

  }

  case class School(override val city: City,
                    override val timeTable: TimeTable,
                    override val openedInLockdown: Boolean,
                    private val classrooms: Seq[Classroom] = Seq())
    extends Institute(city, openedInLockdown, classrooms)


  case class University(override val city: City,
                        override val timeTable: TimeTable,
                        override val openedInLockdown: Boolean,
                        private val classrooms: Seq[Classroom] = Seq())
    extends Institute(city, openedInLockdown, classrooms)


  case class Classroom(private val minCapacity: Int) extends Room with Arrangeable[Student, DeskGroup] {

    override protected val arrangement: DesksArrangement = DesksArrangement.randomFor(minCapacity)

    override val capacity: Int = arrangement.totalCapacity

    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(6, 20),
      RandomGeneration.randomIntInRange(6, 20)
    )

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    /**
     * In Classroom obstacles (i.e. desks) are not considered explicitly,
     * since people motion is not implemented (it is assumed that people remain mostly at their desk).
     * @param dimension the dimension of current space
     * @return          an empty Set
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = Set.empty

    override val mask: Option[Mask] = Some(Masks.Surgical)

    def getStudentDesk(student: Student): Option[Desk] = arrangement.desks.find(_.assignee.contains(student))

    protected override def preEnter(group: Group, time: Calendar): Option[Room] = group.leader match {
      case Teacher(_, _) => Some(this)
      case Student(_, _) => findAccommodation(group) match {
        case Some((_, desks)) if desks.nonEmpty => desks
          .zip(group)
          .foreach(e => e._1.assign(e._2.asInstanceOf[Student]))
          Some(this)
        case _ => None
      }
      case _ => None
    }

    protected override def preExit(group: Group): Unit = {
      group
        .map(p => getStudentDesk(p.asInstanceOf[Student]))
        .collect { case Some(desk) => desk.release() }
    }

    override protected[places] def canEnter(group: Group, time: Calendar): Boolean = group.leader match {
      case Teacher(_,_) =>  true
      case _ => super.canEnter(group, time)
    }

    override def propagateVirus(time: Calendar, place: Location)(covidInfectionParameters: CovidInfectionParameters): Unit = {
      super.propagateVirus(time, place)(covidInfectionParameters)
      if(time.minutes == 0) currentGroups.flatten.toList
        .combinations(2)
        .foreach(pair => VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time))
    }

    override protected[places] def findAccommodation(group: Group): Option[(Room, Seq[Desk])] = Some(
      (this, arrangement.desks.filter(_.isFree).take(group.size))
    )

  }

}
