package pps.covid_sim.model.places.arranging

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.places.FreeTime.{OpenDisco, Pub}
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.OpenPlaces.{Beach, Park, Square}
import pps.covid_sim.model.places.Shops.{ClothesShop, SuperMarket}
import pps.covid_sim.model.places.rooms.{DiscoRoom, GymRoom}
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.MonthsInterval
import pps.covid_sim.util.time.Time.Month

class ObstaclesArrangingTest {

  val cityTest: City = City(1, "Forlì", 118000, Province(1, "FC", "Forlì", Locality.Region.EMILIA_ROMAGNA))
  val timeTable: TimeTable = TimeTable(MonthsInterval(Month.JANUARY, Month.DECEMBER))

  val discoRoom: DiscoRoom = DiscoRoom(50)
  val pub: Pub = Pub(cityTest, timeTable)
  val supermarket: SuperMarket = SuperMarket(cityTest, 50, timeTable)
  val clothesShop: ClothesShop = ClothesShop(cityTest, 10, timeTable)
  val gymRoom: GymRoom = GymRoom(15)
  val openDisco: OpenDisco = OpenDisco(cityTest, timeTable)
  val beach: Beach = Beach(cityTest)
  val square: Square = Square(cityTest)
  val park: Park = Park(cityTest)

  @Test
  def discoRoomObstaclesArranging(): Unit = {
    val discoObstacles = discoRoom.obstacles
    assertFalse(discoObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(discoRoom.dimension)
    println(discoObstacles)
    println("Number of obstacles: " + discoObstacles.size)
    println()
  }

  @Test
  def pubObstaclesArranging(): Unit = {
    val pubObstacles = pub.obstacles
    assertFalse(pubObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(pub.dimension)
    println(pubObstacles)
    println("Number of obstacles: " + pubObstacles.size)
    println()
  }

  @Test
  def supermarketObstaclesArranging(): Unit = {
    val supermarketShelves = supermarket.obstacles
    assertFalse(supermarketShelves.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(supermarket.dimension)
    println(supermarketShelves)
    println("Number of obstacles: " + supermarketShelves.size)
    println()
  }

  @Test
  def clothesShopObstaclesArranging(): Unit = {
    val clothesShopShelves = clothesShop.obstacles
    assertFalse(clothesShopShelves.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(clothesShop.dimension)
    println(clothesShopShelves)
    println("Number of obstacles: " + clothesShopShelves.size)
    println()
  }

  @Test
  def gymRoomObstaclesArranging(): Unit = {
    val gymObstacles = gymRoom.obstacles
    println(gymRoom.dimension)
    println(gymObstacles)
    println("Number of obstacles: " + gymObstacles.size)
    println()
    assertFalse(gymObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
  }

  @Test
  def openDiscoObstaclesArranging(): Unit = {
    val openDiscoObstacles = openDisco.obstacles
    assertFalse(openDiscoObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(openDisco.dimension)
    println(openDiscoObstacles)
    println("Number of obstacles: " + openDiscoObstacles.size)
    println()
  }

  @Test
  def beachObstaclesArranging(): Unit = {
    val beachObstacles = beach.obstacles
    assertFalse(beachObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(beach.dimension)
    println(beachObstacles)
    println("Number of obstacles: " + beachObstacles.size)
    println()
  }

  @Test
  def squareObstaclesArranging(): Unit = {
    val squareObstacles = square.obstacles
    assertFalse(squareObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(square.dimension)
    println(squareObstacles)
    println("Number of obstacles: " + squareObstacles.size)
    println()
  }

  @Test
  def parkObstaclesArranging(): Unit = {
    val parkObstacles = park.obstacles
    assertFalse(parkObstacles.toList.combinations(2).exists(pair => pair.head.vertexes.exists(c => c.inside(pair.last))))
    println(park.dimension)
    println(parkObstacles)
    println("Number of obstacles: " + parkObstacles.size)
    println()
  }

}
