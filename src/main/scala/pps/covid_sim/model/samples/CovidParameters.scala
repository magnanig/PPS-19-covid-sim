package pps.covid_sim.model.samples

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.places.Place

object CovidParameters extends CovidInfectionParameters {

  override val safeZone = 1.5 // meters

  override val minRecoverTime: Int = 7 * 24 // min time (hours) after which patient get recovered
  override val maxRecoverTime: Int = 40 * 24 // max time (hours) after which patient get recovered

  override val minInfectionDetectionTime: Int = 3 * 24

  override val maxInfectionDetectionTime: Int = 14 * 24

  override val multipleInfectionProbability = 0.1

  /**
   * Probability of being asymptomatic, starting from the person's age.
   */
  override val asymptomaticProbability: Int => Double = age => if (age < 40) 0.6 else if (age < 60) 0.5 else 0.2

  /**
   * Probability that an asymptomatic person discovers the infection (conditional probability).
   */
  override val asymptomaticDetectionCondProbability = 0.2

  override val contagionProbability = 0.7

  override val minMaskProbability = 0.3
  override val maxMaskProbability = 1

  override val notRespectingIsolationMaxProbability = 0.4

  override val lockDownStart = 0.1 // population percentage
  override val lockDownEnd = 0.8 // percentage respect with last max infections

  val placeToclose: Set[Class[_ <: Place]] = Set()

}
