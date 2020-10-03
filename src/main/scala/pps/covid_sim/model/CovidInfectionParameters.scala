package pps.covid_sim.model

import pps.covid_sim.model.places.Place

case class CovidInfectionParameters() {

  //TODO fare setter e getter!!!

  private var _safeZone = 1.5 // meters
  private[model] def safeZone_=(distance:Double):Unit = {
    _safeZone = distance
  }
  def safeZone:Double = _safeZone

  var minRecoverTime: Int = 7 * 24 // min time (hours) after which patient get recovered
  var maxRecoverTime: Int = 40 * 24 // max time (hours) after which patient get recovered

  var minInfectionDetectionTime: Int = 3 * 24

  var maxInfectionDetectionTime: Int = 14 * 24

  var multipleInfectionProbability = 0.1

  /**
   * Probability of being asymptomatic, starting from the person's age.
   */
  var asymptomaticProbability: Int => Double = age => if (age < 40) 0.6 else if (age < 60) 0.5 else 0.2

  /**
   * Probability that an asymptomatic person discovers the infection (conditional probability).
   */
  var asymptomaticDetectionCondProbability = 0.2

  var contagionProbability = 0.7

  var minMaskProbability = 0.3
  var maxMaskProbability = 1

  var notRespectingIsolationMaxProbability = 0.4

  var lockDownStart = 0.1 // population percentage
  var lockDownEnd = 0.8 // percentage respect with last max infections

  val placeToclose: Set[Class[_ <:Place]] = Set()

}
