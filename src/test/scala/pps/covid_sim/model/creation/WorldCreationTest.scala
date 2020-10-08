package pps.covid_sim.model.creation

import org.junit.Test
import pps.covid_sim.model.container.{CitiesContainer, PeopleContainer, PlacesContainer}
import pps.covid_sim.model.places.Education.{School, University}
import pps.covid_sim.model.places.FreeTime.{Bar, Disco, OpenDisco, Pub, Restaurant}
import pps.covid_sim.model.places.Hobbies.{FootballTeam, Gym}
import pps.covid_sim.model.places.Jobs.{Company, Factory}
import pps.covid_sim.model.places.OpenPlaces.{Field, Park, Square}
import pps.covid_sim.model.places.Shops.{ClothesShop, SuperMarket}

class WorldCreationTest {

  WorldCreation.create(CitiesObject.getProvince("PP"), Set(classOf[Bar]))

  @Test
  def testCities(): Unit = {
    println("Numero di città create: " + CitiesContainer.getCities.size)
    println(CitiesContainer.getCities)

  }

  @Test
  def testPeople(): Unit = {
    println("Numero di persone create: " + PeopleContainer.people.size)
  }

  @Test
  def testPlaces(): Unit = {
    println("Numero di Piazze create: " + PlacesContainer.getPlaces(classOf[Square]).size)
    println("Numero di Parchi creati: " + PlacesContainer.getPlaces(classOf[Park]).size)
    println("Numero di Campi da calcio creati: " + PlacesContainer.getPlaces(classOf[Field]).size + "\n")

    println("Numero di Scuole create: " + PlacesContainer.getPlaces(classOf[School]).size)
    println("Numero di Università create: " + PlacesContainer.getPlaces(classOf[University]).size + "\n")

    println("Numero di Aziende create: " + PlacesContainer.getPlaces(classOf[Company]).size)
    println("Numero di Fabbriche create: " + PlacesContainer.getPlaces(classOf[Factory]).size + "\n")

    println("Numero di Supermercati creati: " + PlacesContainer.getPlaces(classOf[SuperMarket]).size)
    println("Numero di Negozi di Abbigliamento creati: " + PlacesContainer.getPlaces(classOf[ClothesShop]).size + "\n")

    println("Numero di FootballTeam creati: " + PlacesContainer.getPlaces(classOf[FootballTeam]).size)
    println("Numero di Palestre create: " + PlacesContainer.getPlaces(classOf[Gym]).size + "\n")

    println("Numero di Ristoranti creati: " + PlacesContainer.getPlaces(classOf[Restaurant]).size)
    println("Numero di Bar creati: " + PlacesContainer.getPlaces(classOf[Bar]).size)
    println("Numero di Pub creati: " + PlacesContainer.getPlaces(classOf[Pub]).size)
    println("Numero di Disco create: " + PlacesContainer.getPlaces(classOf[Disco]).size)
    println("Numero di Open Disco create: " + PlacesContainer.getPlaces(classOf[OpenDisco]).size + "\n")
  }
}
