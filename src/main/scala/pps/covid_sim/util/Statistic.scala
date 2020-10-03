package pps.covid_sim.util

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.{Area, City, Province, Region}
import pps.covid_sim.model.places.Place
import pps.covid_sim.parameters.CreationParameters._

import scala.collection.{SortedMap, mutable}
import scala.collection.parallel.ParSeq

object Statistic {

  /**
   * Calculates the minimum value among all input parameters
   *
   * @param elements  elements of type int
   * @return          minimum value among all input parameters
   */
  def getMin(elements: Int*): Int = elements.min

  /**
   * Calculates the average of a list of Int
   *
   * @param  list           list of Int elements whose average value is to be calculated
   * @param  decimalCiphers number of decimal ciphers that must be present
   *                        after the comma in the double value that is returned
   * @return                average of a list in Double value
   * @see                   Resource: https://damieng.com/blog/2014/12/11/sequence-averages-in-scala
   */
  def avgDouble(list: ParSeq[Int], decimalCiphers: Int): Double = {
    val t = list.foldLeft((0, 0)) ((acc, i) => (acc._1 + i, acc._2 + 1))
    BigDecimal(t._1.toDouble / t._2).setScale(decimalCiphers, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  /**
   * Calculates the average of a list of Int
   *
   * @param  list list of Int elements whose average value is to be calculated
   * @return      average of a list rounded and transformed into Int value
   */
  def avgInt(list: ParSeq[Int]): Int = { Math.round(avgDouble(list, 2).toFloat) }

  /**
   * It takes an integer and a list of percentage values expressed as doubles.
   * Returns a list of integer values equal in length to the list of double
   * values taken as input; but converts all percentage values to integers,
   * in proportion to the integer number taken as input.
   *
   * @param number      integer value that must be divided into multiple values
   * @param percentages list of percentage values
   * @return            each int element that is returned represents the quantity
   *                    as a percentage of the respective double element received
   *                    in input, in relation to the integer received in input
   * @throws IllegalArgumentException at least the double elements in input must
   *                                  be two and the sum of all the double
   *                                  elements must be equal to 100.
   */
  def totalPercentageToInt(number: Int, percentages: Double*): List[Int] = {
    require(percentages.size >= 2); require(percentages.sum == oneHundredPercent.toInt)

    var numbers: List[Int] = List()
    percentages.dropRight(1).foreach(percentage =>
      numbers = numbers :+ Math.round((number * (percentage / oneHundredPercent)).toFloat)
    )
    numbers = numbers :+ (number - numbers.sum)
    numbers
  }

  /**
   * It takes an integer and a list of percentage values expressed as doubles.
   * Returns a list of integer values equal in length to the list of double
   * values taken as input; but converts all percentage values to integers,
   * in proportion to the integer number taken as input.
   *
   * @param number      integer value that must be divided into multiple values
   * @param percentages list of percentage values
   * @return            each int element that is returned represents the quantity
   *                    as a percentage of the respective double element received
   *                    in input, in relation to the integer received in input
   */
  def partialPercentageToInt(number: Int, percentages: Double*): List[Int] = {
    var numbers: List[Int] = List()
    percentages.foreach(percentage =>
      numbers = numbers :+ Math.round((number * (percentage / oneHundredPercent)).toFloat)
    )
    numbers
  }

}

/**
 * A class to obtain statistics relating to infections that occur during the evolution of the virus
 */
case class Statistic(people: ParSeq[Person]) {

  /**
   * Calculates the current number of infected for each city.
   *
   * @param cities  cities for which the current number of
   *                infected persons is to be obtained
   * @return        map sorted by the population of each city.
   *                So, at the top of the map there will be cities
   *                with more residents and at the bottom of the
   *                map there will be cities with fewer residents.
   */
  def cityPositives(cities: Set[City]): SortedMap[City, Int] = {
    implicit val cityOrdering: Ordering[City] = (x: City, y: City) => y.numResidents.compareTo(x.numResidents)
    SortedMap[City, Int]() ++ cities.map(city => (city, numCurrentPositive(city))).toMap
  }

  //TODO: scalaDoc
  //paziente 0 lo prende alla forma 0
  //per ogni stadio del virus restituisce quante persone ce l'hanno allo stadio 3, p.e.
  //Da stadio a numero di persone che ce l'hanno per ogni stadio
  def covidStages(): Map[Int, Int] = ???

  /**
   * Calculate the average age of currently positive people
   *
   * @return average age of currently positive people
   */
  def middleAgeCurrentPositive(): Int = {
    Statistic.avgInt(people.par.filter(_.isInfected).map(_.age))
  }

  /**
   * Calculate a map that associates, for each type of place where the virus has spread,
   * the number of people who have been infected.
   *
   * @return a map that associates, for each type of place where the virus has spread,
   *         the number of people who have been infected
   */
  def getInfectionsPerPlace: Map[Class[_ <: Place], Int] = {
    val _return: mutable.Map[Class[_ <: Place], Int] = mutable.Map().withDefaultValue(0)
    people.filter(p => p.isInfected || p.isRecovered).foreach(p => _return(p.infectionPlace.get) += 1)
    _return.toMap
  }

  def numCurrentPositive(area: Area): Int = area match {
    case city: City => numCurrentPositive(city)
    case region: Region => numCurrentPositive(region)
    case province: Province => numCurrentPositive(province)
    case _ => numCurrentPositive
  }

  def numRecovered(area: Area): Int = area match {
    case city: City => numRecovered(city)
    case region: Region => numRecovered(region)
    case province: Province => numRecovered(province)
    case _ => numRecovered
  }

  def numDeaths(area: Area): Int = area match {
    case city: City => numDeaths(city)
    case region: Region => numDeaths(region)
    case province: Province => numDeaths(province)
    case _ => numDeaths
  }

  def numConfirmedCases(area: Area): Int = area match {
    case city: City => numConfirmedCases(city)
    case region: Region => numConfirmedCases(region)
    case province: Province => numConfirmedCases(province)
    case _ => numConfirmedCases
  }

  /**
   * Calculates the total number of currently positive people within a specific city
   *
   * @param c the city in which the number of currently positive people
   *          is calculated
   * @return  number of currently positive people within a city
   */
  private def numCurrentPositive(c: City): Int = people.count(
    person => person.residence == c && person.isInfected)

  /**
   * Calculates the total number of currently positive people within a specific province
   *
   * @param p the province in which the number of currently positive people
   *          is calculated
   * @return  number of currently positive people within a province
   */
  private def numCurrentPositive(p: Province): Int = people.par.count(
    person => person.residence.province == p && person.isInfected)

  /**
   * Calculates the total number of currently positive people within a specific region
   *
   * @param r the region in which the number of currently positive people is
   *          calculated
   * @return  number of currently positive people within a region
   */
  private def numCurrentPositive(r: Region): Int = people.par.count(p => p.residence.province.region == r && p.isInfected)

  /**
   * Calculates the total number of people currently positive across the nation
   *
   * @return total number of people currently positive nationwide
   */
  def numCurrentPositive: Int = people.par.count(_.isInfected)

  /**
   * Calculates the number of people recovered from the virus within a specific city
   *
   * @param c city in which to calculate the number of people recovered from
              the virus
   * @return  number of people recovered from the virus within a specific city
   */
  private def numRecovered(c: City): Int = people.par.count(person => person.residence == c && person.isRecovered)

  /**
   * Calculates the number of people recovered from the virus within a specific province
   *
   * @param p province in which to calculate the number of people recovered from
              the virus
   * @return  number of people recovered from the virus within a specific province
   */
  private def numRecovered(p: Province): Int = people.par.count(person => person.residence.province == p && person.isRecovered)

  /**
   * Calculates the number of people recovered from the virus within a specific region
   *
   * @param r region in which to calculate the number of people recovered from
   *          the virus
   * @return  number of people recovered from the virus within a specific region
   */
  private def numRecovered(r: Region): Int = people.par.count(p => p.residence.province.region == r && p.isRecovered)

  /**
   * Calculates the total number of people recovered from the virus across the nation
   *
   * @return number of people recovered from the virus nationwide
   */
  def numRecovered: Int = people.par.count(_.isRecovered)

  /**
   * Calculate the number of people who have died from the virus within a specific city
   *
   * @param c city in which to calculate the number of people who have died from
   *          the virus
   * @return  number of people who have died from the virus within a specific
   *          city
   */
  private def numDeaths(c: City): Int = people.par.count(person => person.residence == c && person.isDeath)

  /**
   * Calculate the number of people who have died from the virus within a specific province
   *
   * @param p province in which to calculate the number of people who have died from
   *          the virus
   * @return  number of people who have died from the virus within a specific
   *          province
   */
  private def numDeaths(p: Province): Int = people.par.count(person => person.residence.province == p && person.isDeath)

  /**
   * Calculate the number of people who have died from the virus within a specific region
   *
   * @param r region in which to calculate the number of people who have died from
   *          the virus
   * @return  number of people who have died from the virus within a specific
   *          region
   */
  private def numDeaths(r: Region): Int = people.par.count(person => person.residence.province.region == r && person.isDeath)

  /**
   * Calculates the total number of people who have died from the virus across the nation
   *
   * @return number of died people from the virus nationwide
   */
  def numDeaths: Int = people.par.count(_.isDeath)

  /**
   * Calculate the number of confirmed covid cases into city. The calculation includes:
   * the number of people currently positive in the city, the number of people recovered
   * in the city and the number of people who died from the virus within the city
   *
   * @param c city in which to calculate the number of confirmed covid cases
   * @return  number of confirmed cases of covid at the city level
   */
  private def numConfirmedCases(c: City): Int = numCurrentPositive(c) + numRecovered(c) + numDeaths(c)

  /**
   * Calculate the number of provincial confirmed covid cases. The calculation includes:
   * the number of people currently positive in the province, the number of people recovered
   * in the province and the number of people who died from the virus within the province
   *
   * @param p province in which to calculate the number of confirmed covid cases
   * @return  number of confirmed cases of covid at the province level
   */
  private def numConfirmedCases(p: Province): Int = numCurrentPositive(p) + numRecovered(p) + numDeaths(p)

  /**
   * Calculate the number of regionally confirmed covid cases. The calculation includes:
   * the number of people currently positive in the region, the number of people recovered
   * in the region and the number of people who have died from the virus within a region
   *
   * @param r   region in which to calculate the number of confirmed covid cases
   * @return    number of confirmed cases of covid at the regional level
   */
  private def numConfirmedCases(r: Region): Int = numCurrentPositive(r) + numRecovered(r) + numDeaths(r)

  /**
   * Calculate the total number of nationally confirmed covid cases. The calculation
   * includes: the number of people currently positive, the number of people
   * recovered and the number of people who died from the virus
   *
   * @return total number of confirmed cases of covid nationwide
   */
  def numConfirmedCases: Int = numCurrentPositive + numRecovered + numDeaths

}