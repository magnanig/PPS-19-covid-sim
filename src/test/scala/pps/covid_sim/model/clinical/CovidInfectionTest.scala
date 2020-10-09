package pps.covid_sim.model.clinical

import java.util.Calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.samples.CovidParameters
import pps.covid_sim.model.samples.CovidParameters._
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq

class CovidInfectionTest {

  val tolerance: Double = 0.1

  // since working with probability, test with large number to have a better approximation (or use higher tolerance)
  val numPeople: Int = 10000
  val student: Student = Student(ScalaCalendar(2020, 1, 1), null)

  val infectionBegin: Calendar = ScalaCalendar(2020, 1, 1)
  val covidInfections: ParSeq[CovidInfection] = (1 to numPeople).par // better performance on multi-core computers
    .map(_ => CovidInfection(infectionBegin, null, 0, CovidParameters, student))
  val asymptomaticNumber: Int = covidInfections.count(_.isAsymptomatic)

  @Test
  def testNobodyRecoversBeforeMinTime(): Unit = {
    Iterator.iterate(infectionBegin)(_ + 1)
      .take(minRecoverTime - 1)
      .foreach(time => covidInfections.foreach(_.hourTick(time)))
    assert(covidInfections.forall(!_.isRecovered))
  }

  @Test
  def testEveryoneRecoversAfterMaxTime(): Unit = {
    Iterator.iterate(infectionBegin)(_ + 1)
      .take(24 * maxRecoverTime + 1)
      .foreach(time => covidInfections.foreach(_.hourTick(time)))
    assert(covidInfections.forall(_.isRecovered))
  }

  /**
   * Assert the percentage of asymptomatic is about the one specified in parameters, within a tolerance.
   */
  @Test
  def testAsymptomaticPercentage(): Unit = {
    assertEquals(asymptomaticProbability(student.age), asymptomaticNumber / numPeople.toDouble, tolerance)
  }

  /**
   * Assert the percentage of asymptomatic that detects the infection is about the one specified in parameters,
   * within a tolerance.
   */
  @Test
  def testAsymptomaticDetectionPercentage(): Unit = {
    val asymptomaticDetectionProbability = asymptomaticProbability(student.age) * asymptomaticDetectionCondProbability
    Iterator.iterate(infectionBegin)(_ + 1) // let time for all infection detection
      .take(24 * maxInfectionDetectionTime + 1)
      .foreach(time => covidInfections.foreach(_.hourTick(time)))
    assertEquals(asymptomaticDetectionProbability,
      covidInfections.count(c => c.isAsymptomatic && c.infectionKnown) / asymptomaticNumber.toDouble, tolerance)
  }

}
