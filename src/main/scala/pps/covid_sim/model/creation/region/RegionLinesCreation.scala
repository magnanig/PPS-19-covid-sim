package pps.covid_sim.model.creation.region

import pps.covid_sim.model.places.Locality.Region
import pps.covid_sim.model.transports.PublicTransports.Line

object RegionLinesCreation {

  def create(region: Region): Unit = {
    new RegionLinesCreation(region).create()
  }

}

private class RegionLinesCreation(region: Region) {

  def create(): Unit = {

  }

}

/*
private class RegionPlacesCreation(region: Region) {

  private val _people: List[Person] = RegionPeopleCreation.create(region)
  private val random: Random = new Random()

  def create(): List[Place] = {
    PeopleContainer.add(_people)
    val places = _people
      .groupBy(person => person.residence)
      .flatMap(entry => createEntityFor(entry))
      .toList
    //PeopleContainer.checkAssignedWork()
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

    allPlace = allPlace ::: InstitutePlacesCreation().create(entry._1, teachers, students)

    index += workerPerPlace.head + workerPerPlace(1) + workerPerPlace(2)
    allPlace = allPlace ::: WorkPlacesCreation().create(entry._1,
      workerPerPlace.slice(0, 3),
      workers.slice(0, index))

    allPlace = allPlace ::: HobbyPlacesCreation().create(entry._1,
      workerPerHobbyPlace,
      workers.slice(index, index + workerPerPlace(3)), random)
    index += workerPerPlace(3)

    allPlace = allPlace ::: FreeTimePlacesCreation().create(entry._1,
      workerPerFreeTimePlace,
      workers.slice(index, index + workerPerPlace.last), random)

    allPlace = allPlace ::: HabitationsCreation().create(entry._1, entry._2)

    allPlace.foreach(place => PlacesContainer.add(place.city, place))
    allPlace
  }

}
*/
