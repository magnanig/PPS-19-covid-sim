package pps.covid_sim.parameters

object CovidInfectionParameters {

  val safeZone = 1.5 // meters

  val minRecoverTime: Int = 7 * 24 // min time (hours) after which patient get recovered
  val maxRecoverTime: Int = 40 * 24 // max time (hours) after which patient get recovered

  val minInfectionDetectionTime: Int = 3 * 24

  val maxInfectionDetectionTime: Int = 14 * 24

  val multipleInfectionProbability = 0.1

  /**
   * Probability of being asymptomatic, starting from the person's age.
   */
  val asymptomaticProbability: Int => Double = age => if (age < 40) 0.6 else if (age < 60) 0.5 else 0.2

  /**
   * Probability that an asymptomatic person discovers the infection (conditional probability).
   */
  val asymptomaticDetectionCondProbability = 0.2

  val contagionProbability = 0.7

  val minMaskProbability = 0.3
  val maxMaskProbability = 1

  val notRespectingIsolationMaxProbability = 0.4


}
