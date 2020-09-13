package pps.covid_sim.model.transports

import pps.covid_sim.model.people.PeopleGroup.Single
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.time.HoursInterval

object PublicTransports {

  trait Line {

    private var _coveredLocation: Set[Location] = Set()
    val scheduledTime: HoursInterval

    protected final def setCoveredLocation(newSet: Set[Location]): Unit = {
      _coveredLocation = newSet
    }

  }

  trait PublicTransport extends Transport with Line {

  }

  case class Bus(override val capacity: Int,
                 override val scheduledTime: HoursInterval) extends PublicTransport {

  }

  case class Carriage(override val capacity: Int) extends Transport {

    def getCurrentPeopleInCarriage: Int = _numCurrentPeople

  }

  case class Train(carriages: Int,
                   override val scheduledTime: HoursInterval) extends PublicTransport {

    val carriageCapacity: Int = 20
    val carriageList: Seq[Carriage] = (1 to carriages) map (_ => Carriage(carriageCapacity))
    override val capacity: Int = carriageCapacity * carriages

    override def enter(person: Single): Option[Transport] = {
      val ci = (carriageList map (c => c.getCurrentPeopleInCarriage)).zipWithIndex.min._2
      if (carriageList(ci).getCurrentPeopleInCarriage < carriageCapacity && !_peopleSet.contains(person)) {
        _numCurrentPeople += 1
        _peopleSet += person
        carriageList(ci).enter(person)
      } else {
        None
      }
    }

    override def exit(person: Single): Unit = {
      if (_peopleSet.contains(person)) {
        _numCurrentPeople -= 1
        _peopleSet -= person
      }
    }

  }

}
