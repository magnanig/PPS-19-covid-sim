package pps.covid_sim.model.creation

import org.junit.Assert.assertEquals
import org.junit.Test
import pps.covid_sim.model.container.PlacesContainer
import pps.covid_sim.model.places.FreeTime.{Bar, Restaurant}
import pps.covid_sim.model.samples.{Cities, Places, Provinces}
import pps.covid_sim.model.scheduling.TimeTable
import pps.covid_sim.util.time.Time.Day

class PlacesContainerTest {

  val timeTableRestaurant: TimeTable = TimeTable().add(Day.MONDAY)
  val pepeNeroBar: Bar = Bar(Cities.RAVENNA, timeTableRestaurant, openedInLockdown = false)
  pepeNeroBar.addRooms(List(Places.SMALL_ROOM))
  val ilMoroRestaurant: Restaurant = Restaurant(Cities.RAVENNA, timeTableRestaurant, openedInLockdown = false)
  ilMoroRestaurant.addRooms(List(Places.SMALL_ROOM))
  val peccatoDiGolaRestaurant: Restaurant = Restaurant(Cities.CERVIA, timeTableRestaurant, openedInLockdown = false)
  peccatoDiGolaRestaurant.addRooms(List(Places.SMALL_ROOM))

  @Test
  def testPlaceCreation(): Unit = {

    PlacesContainer.add(Cities.RAVENNA, ilMoroRestaurant)
    PlacesContainer.add(Cities.CERVIA, peccatoDiGolaRestaurant)
    assertEquals(0, PlacesContainer.getPlaces(CitiesObject.getProvince("BO"), classOf[Restaurant]).size)
    assertEquals(0, PlacesContainer.getPlaces(Cities.FAENZA, classOf[Restaurant]).size)
    assertEquals(1, PlacesContainer.getPlaces(Cities.CERVIA, classOf[Restaurant]).size)
    assertEquals(2, PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Restaurant]).size)
    assert(PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Restaurant])
      .map(_.asInstanceOf[Restaurant]).contains(ilMoroRestaurant))
    assert(PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Restaurant])
      .map(_.asInstanceOf[Restaurant]).contains(peccatoDiGolaRestaurant))
    assertEquals(0, PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Bar]).size)
    PlacesContainer.add(Cities.RAVENNA, pepeNeroBar)
    assertEquals(1, PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Bar]).size)
    assertEquals(List(pepeNeroBar), PlacesContainer.getPlaces(Provinces.RAVENNA, classOf[Bar]))

    println(Provinces.RAVENNA)
    println(CitiesObject.getProvince("RA"))
  }

}

