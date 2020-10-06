package pps.covid_sim.model

import pps.covid_sim.model.places.Place

case class CovidInfectionParameters() {

  /**
   * Distance of possible infection, if the distance between two person is higher than this there is no risk of contagion
   */
  private var _safeZone = 1.5 // meters
  private[model] def safeZone_=(distance:Double):Unit = {
    _safeZone = distance
  }
  def safeZone:Double = _safeZone

  /**
   * Minimum Days needed to recover from the virus
   */
  private var _minRecoverTime: Int = 7 * 24 // min time (hours) after which patient get recovered
  private[model] def minRecoverTime_=(time :Int):Unit = {
    _minRecoverTime = time
  }
  def minRecoverTime: Int = _minRecoverTime

  /**
   * Maximum Days needed to recover from the virus
   */
  private var _maxRecoverTime: Int = 40 * 24 // max time (hours) after which patient get recovered
  private[model] def maxRecoverTime_=(time :Int):Unit ={
    _maxRecoverTime = time
  }
  def maxRecoverTime: Int = _maxRecoverTime

  /**
   * Minimum Days needed to detect the infection
   */
  private var _minInfectionDetectionTime: Int = 3 * 24
  private[model] def minInfectionDetectionTime_=(time :Int):Unit ={
    _minInfectionDetectionTime = time
  }
  def minInfectionDetectionTime: Int = _minInfectionDetectionTime

  /**
   * Minimum Days needed to detect the infection
   */
  private var _maxInfectionDetectionTime: Int = 14 * 24
  private[model] def maxInfectionDetectionTime_=(time :Int):Unit ={
    _maxInfectionDetectionTime = time
  }
  def maxInfectionDetectionTime: Int = _maxInfectionDetectionTime

  /**
   * Probability of multiple infections
   */
  private var _multipleInfectionProbability: Double = 0
  private[model] def multipleInfectionProbability_=(probability :Double):Unit ={
    _multipleInfectionProbability = probability
  }
  def multipleInfectionProbability: Double = _multipleInfectionProbability

  /**
   * Probability of being asymptomatic, starting from the person's age.
   */
  val asymptomaticProbability: Int => Double = age => if (age < 40) 0.6 else if (age < 60) 0.5 else 0.2

  /**
   * Probability that an asymptomatic person discovers the infection (conditional probability).
   */
  private var _asymptomaticDetectionCondProbability: Double = 0.2
  private[model] def asymptomaticDetectionCondProbability_=(probability :Double):Unit ={
    _asymptomaticDetectionCondProbability = probability
  }
  def asymptomaticDetectionCondProbability: Double = _asymptomaticDetectionCondProbability

  /**
   * Probability of Infection
   */
  private var _contagionProbability: Double = 0.7
  private[model] def contagionProbability_=(probability :Double):Unit ={
    _contagionProbability = probability
  }
  def contagionProbability: Double = _contagionProbability

  /**
   * Minimum probability of a person to wear a mask
   */
  private var _minMaskProbability: Double = 0.3
  private[model] def minMaskProbability_=(probability :Double):Unit ={
    _minMaskProbability = probability
  }
  def minMaskProbability: Double = _minMaskProbability

  /**
   * Maximum probability of a person to wear a mask
   */
  private var _maxMaskProbability: Double = 1.0
  private[model] def maxMaskProbability_=(probability :Double):Unit ={
    _maxMaskProbability = probability
  }
  def maxMaskProbability: Double = _maxMaskProbability


  /**
   * Maximum probability of a person to not respect Isolation, ether lockdown or forced quarantine
   */
  private var _notRespectingIsolationMaxProbability: Double = 0.4
  private[model] def notRespectingIsolationMaxProbability_=(probability :Double):Unit ={
    _notRespectingIsolationMaxProbability = probability
  }
  def notRespectingIsolationMaxProbability: Double = _notRespectingIsolationMaxProbability

  /**
   * percent of people needed to start the lockdown
   */
  private var _lockDownStart: Double = 0.1
  private[model] def lockDownStart_=(probability :Double):Unit ={
    _lockDownStart = probability
  }
  def lockDownStart: Double = _lockDownStart

  /**
   * percent of people needed to end the lockdown
   */
  private var _lockDownEnd: Double = 0.8
  private[model] def lockDownEnd_=(probability :Double):Unit ={
    _lockDownEnd = probability
  }
  def lockDownEnd: Double = _lockDownEnd

  /**
   * Set containig the class of the places that will be closed during Lockdown
   */
  private var _placeToClose: Set[Class[_ <:Place]] = Set()
  private[model] def placeToClose_=(places :Set[Class[_ <:Place]]):Unit ={
    _placeToClose = places
  }
  def placeToClose: Set[Class[_ <:Place]] = _placeToClose

}
