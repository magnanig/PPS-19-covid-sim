package pps.covid_sim.model.creation

import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.{Habitation, Place}
import pps.covid_sim.util.RandomGeneration

import scala.util.Random

private[creation] case class HabitationsCreation(){

  private val averageNumPeople = 4

  def create(city: City, people: List[Person]): List[Place] = {
    var habitations: List[Place] = List()
    val remainingPeople = Random.shuffle(people).toIterator
    while (remainingPeople.hasNext) {
      val habitation = Habitation(city)
      remainingPeople
        .take(RandomGeneration.randomIntFromGaussian(averageNumPeople, 3, 1))
        .foreach(person => {
          person.setHabitation(habitation)
          habitation.addMember(person)
        })
      // Note: take(n) + foreach, will result in the drop of the first n items from original iterator,
      // even though it's a val
      habitations = habitation :: habitations
    }
    habitations
  }

}