package pps.covid_sim.model.creation

import pps.covid_sim.util.Statistic
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.people.Person
import pps.covid_sim.parameters.CreationParameters._
import pps.covid_sim.model.places.Locality.{City, Region}
import pps.covid_sim.model.people.People.{Student, Teacher, Worker}

import scala.util.Random

// TODO: Scala Doc

object PlacesCreation {

  private var places: List[Place] = List()

  def create(region: Region): Unit = { if (places.isEmpty) places = new PlacesCreation(region).create() }

}

private class PlacesCreation(region: Region) {

  private val people: List[Person] = PeopleCreation.create(region)
  private val random: Random = new Random()

  def create(): List[Place] = {
    val places = people
      .groupBy(person => person.residence)
      .flatMap(entry => createEntityFor(entry))
      .toList
    PeopleCreation.checkAssignedWork()
    places
  }

  def createEntityFor(entry: (City, List[Person])): List[Place] = {
    var allPlace: List[Place] = List()
    var index: Int = 0

    val teachers: List[Teacher] = entry._2.filter(_.getClass == classOf[Teacher]).map(_.asInstanceOf[Teacher])

    val students: List[Student] = entry._2.filter(_.getClass == classOf[Student]).map(_.asInstanceOf[Student])

    val workers: List[Worker] = entry._2.filter(_.getClass == classOf[Worker]).map(_.asInstanceOf[Worker])

    val workerPerPlace: List[Int] = Statistic.totalPercentageToInt(workers.size, companyPercentage, factoryPercentage,
      shopPercentage, hobbyPercentage, freeTimePlacePercentage)

    val workerPerHobbyPlace: List[Int] = Statistic.totalPercentageToInt(workerPerPlace(3), footballTeamPercentage,
      gymPercentage)

    val workerPerFreeTimePlace: List[Int] = Statistic.totalPercentageToInt(workerPerPlace.last, restaurantsPercentage,
      barPercentage, pubPercentage, discoPercentage, openDiscoPercentage)

    allPlace = allPlace ::: OpenPlacesCreation().create(entry._1)

    allPlace
  }

}