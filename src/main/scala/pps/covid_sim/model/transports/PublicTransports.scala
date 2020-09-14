package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations
import pps.covid_sim.util.time.HoursInterval

object PublicTransports {

  trait Line {

    private var _coveredCities: Set[City] = Set()
    val scheduledTime: HoursInterval

    protected final def setCoveredCities(newSet: Set[City]): Unit = {
      _coveredCities = newSet
    }

  }

  case class BusLine(buses: Int,
                     capacity: Int,
                     override val scheduledTime: HoursInterval) extends Line {

    val busSet: Set[Bus] = ((0 to buses) map (_ => Bus(capacity, scheduledTime))).toSet
  }

  case class TrainLine(trains: Int,
                       carriages: Int,
                       override val scheduledTime: HoursInterval) extends Line {

    val trainSet: Set[Train] = ((0 to trains) map (_ => Train(carriages, scheduledTime))).toSet

  }

  trait PublicTransport extends Transport with Line {

  }

  case class Bus(override val capacity: Int,
                 override val scheduledTime: HoursInterval) extends PublicTransport {

  }

  case class Carriage(override val capacity: Int) extends Transport {

  }

  case class Train(carriages: Int,
                   override val scheduledTime: HoursInterval) extends PublicTransport {

    val carriageCapacity: Int = 20
    val carriageList: Seq[Carriage] = (1 to carriages) map (_ => Carriage(carriageCapacity))
    override val capacity: Int = carriageCapacity * carriages

    override def preEnter(group: Group, time: Calendar): Option[Locations.LimitedPeopleLocation] = {
      val ci = (carriageList map (c => c.numCurrentPeople)).zipWithIndex.min._2
      carriageList(ci).enter(group, time)
      Some(carriageList(ci))
    }

    override def preExit(group: Group): Unit = {
      if (currentGroups.contains(group)) {
        carriageList.foreach(c => {
          if (c.currentGroups.contains(group)) {
            c.exit(group)
          }
        })
      }
    }
  }



}
