package pps.covid_sim.parameters

import pps.covid_sim.model.places.FreeTime._
import pps.covid_sim.model.places.OpenPlaces.{Field, Park, Square}
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.Shops.ClothesShop
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.time.DaysInterval
import pps.covid_sim.util.time.Time.Season.{SUMMER, Season}

object GoingOutParameters {

  val maxGoingOutTimes: (Season, DaysInterval, Int) => Int = (season, daysInterval, age) => {
    val young = 40
    season match {
      case SUMMER if daysInterval == DaysInterval.WEEK => if (age < young) 5 else 3
      case SUMMER if daysInterval == DaysInterval.WEEKEND => if (age < young) 2 else 1
      case _ if daysInterval == DaysInterval.WEEK => if (age < young) 4 else 2
      case _ => if (age < young) 2 else 1
    }
  }

  val averageOldPeopleGoingOutTimes: Double = 1.0 / 7

  def placesPreferences(age: Int): Map[Class[_ <: Place], Double] = Map[Class[_ <: Place], (Double, Double)](
    classOf[Restaurant] -> (0.2, 1),
    classOf[Bar] -> (0.3, 1),
    classOf[Disco] -> (0, if (age > 60) 0.0 else if (age < 40) 1.0 else 0.4),
    classOf[OpenDisco] -> (0, if (age > 60) 0.0 else if (age < 40) 1.0 else 0.5),
    classOf[Pub] -> (0, if (age > 60) 0.0 else if (age < 40) 1.0 else 0.5),
    classOf[Field] -> (0, if (age < 40) 0.5 else 0.0),
    classOf[ClothesShop] -> (0.1, 0.5),
    classOf[Square] -> (0.3, 1),
    classOf[Park] -> (0.2, 1)
  ).map(e => e._1 -> RandomGeneration.randomDoubleInRange(e._2._1, e._2._2)).toMap

  val maxNumShopPerWeek = 5

}
