package pps.covid_sim.model.creation

import org.junit.Test
import org.junit.Assert.assertEquals
import pps.covid_sim.util.Statistic
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.Region
import pps.covid_sim.parameters.CreationParameters._
import pps.covid_sim.model.creation.samples.PeopleAosta
import pps.covid_sim.model.people.People.{Student, Teacher, Unemployed, Worker}

class RegionPeopleCreationTest {

  val peopleAosta: List[Person] = PeopleAosta.people

  val totalWorker: List[Person] = peopleAosta.filter(_.getClass == classOf[Worker])
  val totalTeacher: List[Person] = peopleAosta.filter(_.getClass == classOf[Teacher])
  val totalStudent: List[Person] = peopleAosta.filter(_.getClass == classOf[Student])
  val totalUnemployed: List[Person] = peopleAosta.filter(_.getClass == classOf[Unemployed])

  val totalPersonExpected: List[Int] = Statistic.totalPercentageToInt(Region.VALLE_DAOSTA.numResidents, //127.844
    workersPercentage, teachersPercentage, studentsPercentage, unemployedPercentage)

  val workerInAllein: List[Person] = peopleAosta
    .filter(p => p.residence.name.equals("Allein") && p.getClass == classOf[Worker]) // per evitare le sottoclassi
  val teacherInAllein: List[Person] = peopleAosta
    .filter(p => p.residence.name.equals("Allein") && p.getClass == classOf[Teacher])
  val studentInAllein: List[Person] = peopleAosta
    .filter(p => p.residence.name.equals("Allein") && p.getClass == classOf[Student])
  val unemployedInAllein: List[Person] = peopleAosta
    .filter(p => p.residence.name.equals("Allein") && p.getClass == classOf[Unemployed])

  val personNumberExpected: List[Int] = Statistic.totalPercentageToInt(244,
    workersPercentage, teachersPercentage, studentsPercentage, unemployedPercentage)

  @Test
  def testPeopleCreation(): Unit = {
    assertEquals(Locality.Region.VALLE_DAOSTA.numResidents, peopleAosta.size)
    assertEquals(personNumberExpected,
      List(workerInAllein.size, teacherInAllein.size, studentInAllein.size, unemployedInAllein.size))
    assertEquals(totalPersonExpected.sum,
      List(totalWorker.size, totalTeacher.size, totalStudent.size, totalUnemployed.size).sum)
  }
}
