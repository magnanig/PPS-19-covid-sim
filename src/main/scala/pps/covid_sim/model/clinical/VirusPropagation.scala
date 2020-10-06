package pps.covid_sim.model.clinical

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locations.Location

import scala.util.Random

case class VirusPropagation(covidInfectionParameters: CovidInfectionParameters) {

  /**
   * Try to infect one of the two people, only if one is infected and the other not.
   * If no implicit distance is found, then will be used the min social distance field
   * of Person.
   * @param p1              the first person
   * @param p2              the second person
   * @param place           the place where the two people are
   * @param time            the current time
   * @param socialDistance  an implicit representing the distance between two people, if not
   *                        found will be used the minimum between the two people social distance
   *                        field
   */
  def tryInfect(p1: Person, p2: Person, place: Location, time: Calendar)
               (implicit socialDistance: Double = Math.min(p1.socialDistance, p2.socialDistance)): Unit = {
    if (p1.canInfect != p2.canInfect && !inSafeZone(socialDistance)) {
      val infectedPerson = if (p1.canInfect) p1 else p2
      val healthyPerson = if (infectedPerson eq p1) p2 else p1
      if (!healthyPerson.infectedPeopleMet.contains(infectedPerson) &&
        healthyPerson.canBeInfected(covidInfectionParameters.multipleInfectionProbability)) {
        val contagionProbability = covidInfectionParameters.contagionProbability *
          infectionReducingFactor(socialDistance) *
          (1 - infectedPerson.wornMask.map(_.outgoingFiltering).getOrElse(0.0)) *
          (1 - healthyPerson.wornMask.map(_.incomingFiltering).getOrElse(0.0))
        if (new Random().nextDouble() < contagionProbability){
          healthyPerson.infects(place, time, infectedPerson.covidStage.get + 1)(covidInfectionParameters)
        } else {
          healthyPerson.metInfectedPerson(infectedPerson)
        }
      }
    }
  }

  def inSafeZone(distance: Double): Boolean = distance > covidInfectionParameters.safeZone

  // values to be properly deducted from
  //  https://fastlifehacks.com/n95-vs-ffp/,
  //  https://vimeo.com/402577241
  //  https://medium.com/@Cancerwarrior/covid-19-why-we-should-all-wear-masks-there-is-new-scientific-rationale-280e08ceee71

  //considering that droplets does not spread over 6 meters but the most travel at most 2 meters and at leas 1,5
  //80 - 20
  /**
   * the factor that reduce the probability of being infected based on the distance
   * @param distance to consider
   * @return the factor that will be multiplied to the actual probability
   */
  def infectionReducingFactor(distance: Double): Double = {
    if (distance>6){
      0
    } else if(distance>2){
      1-(distance/6 )//0.77max 0min
    }else{
      if(distance > 0){
        1-(distance/8.8)
      }else{
        1
      }
    }
  }
}
