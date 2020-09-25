package pps.covid_sim.model.creation

import org.junit.Test
import org.junit.Assert.assertEquals
import pps.covid_sim.model.places.Locality.{City, Region}

import scala.collection.mutable

class CitiesCreationTest {

  val cities: Set[City] = CitiesObject.getCities

  val numCities: Int = {
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    val lines: Int = bufferedSource.getLines.size
    bufferedSource.close()
    lines
  }

  @Test
  def testCitiesCreation(): Unit = {
    assertEquals(numCities, cities.size)
    assertEquals(20, CitiesObject.getRegions.size)
    assertEquals(1, cities.count(city => city.name.equals("Cervia")))
    assertEquals(18, CitiesObject.getCities("RA").size)
    assertEquals(9, CitiesObject.getProvince(Region.EMILIA_ROMAGNA).size)
  }

  /**
   * Test of the CitiesObject.getCities(region: Region) method. The goal is to
   * understand if the method returns exactly all the cities of each region. In this
   * regard, two files were used: testOriginal.txt in which the cities we expect to
   * have are stored, while in testQuery.txt the cities that are returned by the
   * method we are testing are stored.
   * In case all cities in all regions are correct,the test passes.
   */
  @Test
  def testGetCities(): Unit = {
    CitiesObject.getRegions.foreach(region => testCities(region))
  }

  private def testCities(inputRegion: Region): Unit = {
    import java.io._
    val writeOriginal = new PrintWriter(new File("res/testOriginal.txt"))
    val writeQuery = new PrintWriter(new File("res/testQuery.txt"))
    val bufferedSource = io.Source.fromFile("res/italy_cities.csv")
    // get the cities directly from the original file .csv
    var expectedCities: mutable.ListBuffer[String] = mutable.ListBuffer()
    for (line <- bufferedSource.getLines) {
      val Array(_, common, _, region, _, _, _) = line.split(";")
      if (region == inputRegion.name) expectedCities += common
    }
    expectedCities.sorted.foreach(elem => writeOriginal.write(elem + "\n"))
    // get the cities from the CitiesObject object
    CitiesObject.getCities(inputRegion).toList.sortBy(city => city.name)
      .foreach(city => writeQuery.write(city.name + "\n"))

    bufferedSource.close
    writeOriginal.close()
    writeQuery.close()

    val buffOriginal = io.Source.fromFile("res/testOriginal.txt")
    val buffQuery = io.Source.fromFile("res/testQuery.txt")
    //Comparing two files simultaneously, line by line
    for ((expected, query) <- buffOriginal.getLines().zip(buffQuery.getLines())) assertEquals(expected, query)
    buffOriginal.close()
    buffQuery.close()
  }
}
