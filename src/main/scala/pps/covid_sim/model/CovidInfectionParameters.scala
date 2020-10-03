package pps.covid_sim.model

import pps.covid_sim.model.places.Place

case class CovidInfectionParameters() {

  //TODO fare setter e getter!!!

  private var _safeZone = 1.5 // meters
  private[model] def safeZone_=(distance:Double):Unit = {
    _safeZone = distance
  }
  def safeZone:Double = _safeZone

  private var _minRecoverTime: Int = 7 * 24 // min time (hours) after which patient get recovered
  private[model] def minRecoverTime_=(time :Int):Unit = {
    _minRecoverTime = time
  }
  def minRecoverTime: Int = _minRecoverTime

  private var _maxRecoverTime: Int = 40 * 24 // max time (hours) after which patient get recovered
  private[model] def maxRecoverTime_=(time :Int):Unit ={
    _maxRecoverTime = time
  }
  def maxRecoverTime: Int = _maxRecoverTime


  private var _minInfectionDetectionTime: Int = 3 * 24
  private[model] def minInfectionDetectionTime_=(time :Int):Unit ={
    _minInfectionDetectionTime = time
  }
  def minInfectionDetectionTime: Int = _minInfectionDetectionTime

  private var _maxInfectionDetectionTime: Int = 14 * 24
  private[model] def maxInfectionDetectionTime_=(time :Int):Unit ={
    _maxInfectionDetectionTime = time
  }
  def maxInfectionDetectionTime: Int = _maxInfectionDetectionTime

  private var _multipleInfectionProbability: Double = 0.1
  private[model] def multipleInfectionProbability_=(probability :Double):Unit ={
    _multipleInfectionProbability = probability
  }
  def multipleInfectionProbability: Double = _multipleInfectionProbability

  /**
   * Probability of being asymptomatic, starting from the person's age.
   */
  var asymptomaticProbability: Int => Double = age => if (age < 40) 0.6 else if (age < 60) 0.5 else 0.2

  /**
   * Probability that an asymptomatic person discovers the infection (conditional probability).
   */

  private var _asymptomaticDetectionCondProbability: Double = 0.2
  private[model] def asymptomaticDetectionCondProbability_=(probability :Double):Unit ={
    _asymptomaticDetectionCondProbability = probability
  }
  def asymptomaticDetectionCondProbability: Double = _asymptomaticDetectionCondProbability

  private var _contagionProbability: Double = 0.7
  private[model] def contagionProbability_=(probability :Double):Unit ={
    _contagionProbability = probability
  }
  def contagionProbability: Double = _contagionProbability


  private var _minMaskProbability: Double = 0.3
  private[model] def minMaskProbability_=(probability :Double):Unit ={
    _minMaskProbability = probability
  }
  def minMaskProbability: Double = _minMaskProbability


  private var _maxMaskProbability: Double = 1.0
  private[model] def maxMaskProbability_=(probability :Double):Unit ={
    _maxMaskProbability = probability
  }
  def maxMaskProbability: Double = _maxMaskProbability


  private var _notRespectingIsolationMaxProbability: Double = 0.4
  private[model] def notRespectingIsolationMaxProbability_=(probability :Double):Unit ={
    _notRespectingIsolationMaxProbability = probability
  }
  def notRespectingIsolationMaxProbability: Double = _notRespectingIsolationMaxProbability


  private var _lockDownStart: Double = 0.1
  private[model] def lockDownStart_=(probability :Double):Unit ={
    _lockDownStart = probability
  }
  def lockDownStart: Double = _lockDownStart




  private var _lockDownEnd: Double = 0.8
  private[model] def lockDownEnd_=(probability :Double):Unit ={
    _lockDownEnd = probability
  }
  def lockDownEnd: Double = _lockDownEnd


  private var _placeToclose: Set[Class[_ <:Place]] = Set()
  private[model] def placeToclose_=(places :Set[Class[_ <:Place]]):Unit ={
    _placeToclose = places
  }
  def placeToclose: Set[Class[_ <:Place]] = _placeToclose


  //val placeToclose: Set[Class[_ <:Place]] = Set()

}
