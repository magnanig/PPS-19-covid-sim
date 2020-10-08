package pps.covid_sim.model.creation

import org.junit.Test
import pps.covid_sim.model.places.Locality.Italy

class WorldCreationTest {

  @Test
  def myTest(): Unit = {
    WorldCreation.create(Italy(), Set())
  }
}
