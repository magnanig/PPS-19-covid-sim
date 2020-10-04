package pps.covid_sim.model.clinical

import java.util.Calendar

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Place
import pps.covid_sim.parameters.CovidInfectionParameters

import scala.util.Random

object VirusPropagation {

  def tryInfect(p1: Person, p2: Person, place: Place, time: Calendar)
               (implicit socialDistance: Double = Math.min(p1.socialDistance, p2.socialDistance)): Unit = {
    if (p1.canInfect != p2.canInfect && !inSafeZone(socialDistance)) {
      val infectedPerson = if (p1.canInfect) p1 else p2
      val healthyPerson = if (infectedPerson eq p1) p2 else p1
      if (!healthyPerson.infectedPeopleMet.contains(infectedPerson)) {
        val contagionProbability = CovidInfectionParameters.contagionProbability *
          infectionReducingFactor(socialDistance) *
          (1 - infectedPerson.wornMask.map(_.outgoingFiltering).getOrElse(0.0)) *
          (1 - healthyPerson.wornMask.map(_.incomingFiltering).getOrElse(0.0))
        if (new Random().nextDouble() < contagionProbability){
          healthyPerson.infects(place, time, infectedPerson.covidStage.get)
        } else {
          healthyPerson.metInfectedPerson(infectedPerson)
        }
      }
    }
  }

  def inSafeZone(distance: Double): Boolean = distance > CovidInfectionParameters.safeZone

  // TODO: values to be properly deducted from
  //  https://fastlifehacks.com/n95-vs-ffp/,
  //  https://vimeo.com/402577241
  //  https://medium.com/@Cancerwarrior/covid-19-why-we-should-all-wear-masks-there-is-new-scientific-rationale-280e08ceee71

  def infectionReducingFactor(distance: Double): Double = 1 // TODO

}
