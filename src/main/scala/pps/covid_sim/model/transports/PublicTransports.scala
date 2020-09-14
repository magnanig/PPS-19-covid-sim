package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.{PeopleGroup, Person}
import pps.covid_sim.model.places.Locality.{City, Province}
import pps.covid_sim.model.places.{Locations, Place}
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.util.time.HoursInterval

object PublicTransports {

  trait Line {

    private var _coveredCities: Set[City] = Set()
    val scheduledTime: HoursInterval

    def setCoveredCities(newSet: Set[City]): Unit = {
      _coveredCities = newSet
    }

    def tryUse(group: Group, time:Calendar): Any

    def isOpen(hour: Int): Boolean = {
      if (hour >= 0  && hour < 24) {
        scheduledTime.contains(hour)
      }else{
        false
      }
    }

    /**
     * Method that inform if the line can carry the person to an specified location.
     * @param location desired destination.
     * @return if the line cover that area.
     */
    def isReachable(location: Place): Boolean = {
      _coveredCities.contains(location.city)
    }

  }

  /**
   *
   * @param buses quantity of buses available for the line
   * @param capacity People that buses can carry for the travel
   * @param scheduledTime The interval of time where the line is available
   */
  case class BusLine(buses: Int,
                     capacity: Int,
                     override val scheduledTime: HoursInterval) extends Line {

    //TODO trasformo momentaneamente in lista per il test
    val busSet: Seq[Bus] = ((1 to buses) map (_ => Bus(capacity)))

    /**
     *
     * @param group of people that want to use the line and ask for a aviable bus
     * @param time in which it is required to use the line
     * @return an Option of Bus that is the available Public transport the group will use to travel.
     *         If there is no Public Transport available in the line a None is returned.
     */
    override def tryUse(group: Group, time:Calendar): Option[PublicTransport] =  {
      if (this.isOpen(time.getTime.getHours)) {
        val availableBuses = busSet.filter(b => b.capacity - b.numCurrentPeople >= group.size)
        if (!availableBuses.isEmpty) {
          val selectedMeans: Bus = availableBuses min Ordering[Int].on[Bus] ( _.numCurrentPeople );
          selectedMeans.enter(group, time)
          return Some(selectedMeans)
        }
      }
      None
    }
  }

  case class TrainLine(trains: Int,
                       carriages: Int,
                       override val scheduledTime: HoursInterval) extends Line {

    val trainSet: Seq[Train] = (1 to trains) map (_ => Train(carriages))

    /*def isReachable(location: Place): Boolean = {
      _coveredCities.contains(location.city)
    }*/

    //def isReachable(location: Province): Boolean = ??? //probabilmente bisogna avere anche il set di provincie raggiungibili che poi sono tutte quelle di una regione quindi magari basta avere solo la regione di riferimento, possiamo passargliela alla creazione dalla line

    /**
     *
     * @param group
     * @param time
     * @return a pair consisting of the train and carriage available
     */
    override def tryUse(group: Group, time: Calendar): (Option[PublicTransport], Option[Transport]) = {
      if (this.isOpen(time.getTime.getHours)) {
        val availableTrains = trainSet.filter(t => t.capacity - t.numCurrentPeople >= group.size)
        if (!availableTrains.isEmpty) {

          availableTrains.head.enter(group, time)

          //TODO forse bisognerebbe fare l'enter anche del carriage e togliere l'auto enter del treno da riga 107.. lascio valutare a te ma mi sembra la cosa migliore.
          val availableCarriage = availableTrains.head.carriageList.filter(c => c.capacity - c.numCurrentPeople >= group.size)
          val selectedCarriage: Carriage = availableCarriage min Ordering[Int].on[Carriage] ( _.numCurrentPeople );
          val availableTrain = Some(availableTrains.head)
          return (availableTrain, Some(selectedCarriage))
        }
      }
      (None, None)
    }
  }

  trait PublicTransport extends Transport {

  }

  case class Bus(override val capacity: Int) extends PublicTransport {
  }

  case class Carriage(override val capacity: Int) extends Transport {

  }

  case class Train(carriages: Int) extends PublicTransport {

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
