package pps.covid_sim.util

import java.util.Calendar

import pps.covid_sim.parameters.CreationParameters

import scala.annotation.tailrec
import scala.util.Random

object RandomGeneration {

  /**
   * Generate a random integer in a closed range (inclusive).
   * @param min       the minimum inclusive value
   * @param max       the maximum inclusive value
   * @param random    an optional Random object to be used to generate a random number
   * @return          a random integer between min and max (or max and min, if not in order)
   */
  @tailrec // at most once
  def randomIntInRange(min: Int, max: Int, random: Random = new Random()): Int = if(max >= min)
    min + random.nextInt(max + 1 - min) else randomIntInRange(max, min, random)

  /**
   * Generate a random double in a range.
   * @param min       the minimum inclusive value
   * @param max       the maximum exclusive value (if not specified it is equals to 1)
   * @param random    an optional Random object to be used to generate a random number
   * @return          a random double between min and max (or max and min, if not in order)
   */
  @tailrec // at most once
  def randomDoubleInRange(min: Double, max: Double = 1, random: Random = new Random()): Double = if(max >= min)
    min + random.nextDouble() * (max - min) else randomDoubleInRange(max, min, random)

  /**
   * Generate a random double from a gaussian distribution.
   *
   * @param avg       the gaussian average
   * @param stdDev the gaussian standard deviation
   * @param random    an optional Random object to be used to generate a random number
   * @return          a random double from the specified gaussian
   */
  def randomDoubleFromGaussian(avg: Double, stdDev: Double, min: Double = 0, random: Random = new Random()): Double = {
    Math.max(min, random.nextGaussian() * stdDev + avg)
  }

  /**
   * Generate a random integer from a gaussian distribution.
   *
   * @param avg       the gaussian average
   * @param stdDev    the gaussian standard deviation
   * @param min a lower bound for the generated number
   * @param random    an optional Random object to be used to generate a random number
   * @return          a random integer from the specified gaussian
   */
  def randomIntFromGaussian(avg: Int, stdDev: Int, min: Int = 0, random: Random = new Random()): Int = Math.round(
    Math.max(min, randomDoubleFromGaussian(avg, stdDev, min, random))
  ).toInt

  /**
   * Generates a random date of birth between min and max years.
   *
   * @param random  an optional Random object to be used to generate a random number
   * @return        a random date of birth between min and max years
   */
  def randomBirthDate(min: Int, max: Int, random: Random = new Random()): Calendar = {
    val birthDate: Calendar = Calendar.getInstance()
    birthDate.add(Calendar.DAY_OF_MONTH, -randomIntInRange(min, max * CreationParameters.daysInYear, random))
    birthDate
  }

}
