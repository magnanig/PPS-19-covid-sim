package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.{PeopleGroup, Person}
import pps.covid_sim.model.places.Locality.{City, Province, Region}
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
     * Method that inform if the line can carry the person to a specified location.
     * @param location the desired destination
     * @return true if the line cover that area
     */
    def isReachable(location: Place): Boolean = {
      _coveredCities.contains(location.city)
    }

  }

  /**
   *
   * @param buses amount of buses available for the line
   * @param capacity amount of people that buses can carry for the travel
   * @param scheduledTime the interval of time in which the line is available
   */
  case class BusLine(buses: Int,
                     capacity: Int,
                     override val scheduledTime: HoursInterval) extends Line {

    val busList: Seq[Bus] = (1 to buses) map (_ => Bus(capacity))

    /**
     *
     * @param group of people that want to use the line and ask for an available bus
     * @param time in which it is required to use the line
     * @return an Option of Bus that is the available public transport the group will use to travel.
     *         If there is no public transport available in the line None is returned.
     */
    override def tryUse(group: Group, time:Calendar): Option[PublicTransport] =  {
      if (this.isOpen(time.getTime.getHours) && !this.isOneMemberAlreadyIn(group)) {
        val availableBuses = busList.filter(b => b.capacity - b.numCurrentPeople >= group.size)
        if (availableBuses.nonEmpty) {
          val selectedMeans: Bus = availableBuses min Ordering[Int].on[Bus] (_.numCurrentPeople)
          selectedMeans.enter(group, time)
          return Some(selectedMeans)
        }
      }
      None
    }

    def isOneMemberAlreadyIn(group: Group): Boolean = {
      //val peopleUsingLine = busList.flatMap(bus => bus.currentGroups.toList).flatMap(group => group.people.toList).toList
      //group.exists(person => peopleUsingLine.contains(person))
      group.exists(person => busList.flatMap(bus => bus.currentGroups.toList).flatMap(group => group.people.toList).toList.contains(person))
    }
  }

  case class TrainLine(trains: Int,
                       carriages: Int,
                       coveredRegion: Region,
                       override val scheduledTime: HoursInterval) extends Line {

    val trainList: Seq[Train] = (1 to trains) map (_ => Train(carriages))

    /**
     *
     * @param group
     * @param time
     * @return a pair consisting of the train and carriage available
     */
    override def tryUse(group: Group, time: Calendar): (Option[PublicTransport], Option[Transport]) = {
      if (this.isOpen(time.getTime.getHours)) {
        val availableTrains = trainList.filter(t => t.capacity - t.numCurrentPeople >= group.size)
        if (availableTrains.nonEmpty) {

          val availableTrain = Some(availableTrains.head)
          availableTrain.get.enter(group, time)

          //vado a trovare dove è stato inserito il gruppo:
          val selectedCarriage = availableTrain.get.carriageList.filter(c => c.currentGroups.contains(group)).head

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
