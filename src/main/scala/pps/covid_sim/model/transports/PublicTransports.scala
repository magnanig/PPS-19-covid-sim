package pps.covid_sim.model.transports

import java.util.Calendar

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.{City, Region}
import pps.covid_sim.model.places.{Locations, Place}
import pps.covid_sim.util.time.HoursInterval
import pps.covid_sim.util.time.Time.ScalaCalendar

/**
 * Different kind of public transports.
 */
object PublicTransports {

  /**
   * Public transports line, covering a set of city, within a certain area.
   */
  trait Line {

    private var _coveredCities: Set[City] = Set()
    val scheduledTime: HoursInterval

    /**
     * Method called to set the cities covered from the line.
     * @param newSet of cities that will be covered
     */
    def setCoveredCities(newSet: Set[City]): Unit = {
      _coveredCities = newSet
    }

    /**
     * Attempt to use a line.
     * @param group     group of people who want to get on board
     * @param time      in which it is required to use the line
     * @return
     */
    def tryUse(group: Group, time:Calendar): Any

    /**
     * Informs about the availability of the line in the specified hour.
     * @param hour      to check if the line is available
     * @return          true if the line is available, false otherwise
     */
    def isOpen(hour: Int): Boolean = {
      if (hour >= 0  && hour < 24) {
        scheduledTime.contains(hour)
      } else {
        false
      }
    }

    /**
     * Method that informs if the line can carry the person to a specified location.
     * @param location      the desired destination
     * @return              true if the line covers that area, false otherwise
     */
    def isReachable(location: City): Boolean = {
      _coveredCities.contains(location)
    }

  }

  /**
   * Class that model a bus line.
   * @param buses           amount of buses available for the line
   * @param capacity        amount of people that buses can carry for the travel
   * @param scheduledTime   the interval of time in which the line is available
   */
  case class BusLine(buses: Int,
                     capacity: Int,
                     override val scheduledTime: HoursInterval) extends Line {

    val busList: Seq[Bus] = (1 to buses) map (_ => Bus(capacity))

    /**
     * Attempt to use the bus line.
     * @param group     of people that want to use the line and ask for an available bus
     * @param time      in which it is required to use the line
     * @return          an Option of Bus that is the available public transport the group will use to travel.
     *                  If there is no public transport available in the line None is returned.
     */
    override def tryUse(group: Group, time:Calendar): Option[PublicTransport] =  {
      if (this.isOpen(time.hour) && !this.isOneMemberAlreadyIn(group)) {
        val availableBuses = busList.filter(b => b.capacity - b.numCurrentPeople >= group.size)
        if (availableBuses.nonEmpty) {
          val selectedMeans: Bus = availableBuses min Ordering[Int].on[Bus] (_.numCurrentPeople)
          selectedMeans.enter(group, time)
          return Some(selectedMeans)
        }
      }
      None
    }

    /**
     * Check if a group member is already using the line.
     * @param group     the group asking to use the line
     * @return          true if a group member is already using the line, false otherwise
     */
    def isOneMemberAlreadyIn(group: Group): Boolean = {
      group.exists(person => busList.flatMap(bus => bus.currentGroups.toList)
        .flatMap(group => group.people.toList).toList.contains(person))
    }
  }

  /**
   * Class that model a train line.
   * @param trains          amount of trains available for the line
   * @param carriages       amount of carriages for each train
   * @param coveredRegion   the regions covered by the line
   * @param scheduledTime   the interval of time in which the line is available
   */
  case class TrainLine(trains: Int,
                       carriages: Int,
                       coveredRegion: Region,
                       override val scheduledTime: HoursInterval) extends Line {

    val trainList: Seq[Train] = (1 to trains) map (_ => Train(carriages))

    /**
     * Attempt to use the train line.
     * @param group     of people that want to use the line and ask for an available train
     * @param time      in which it is required to use the line
     * @return          a pair consisting of the train and carriage available
     */
    override def tryUse(group: Group, time: Calendar): (Option[PublicTransport], Option[Transport]) = {
      if (this.isOpen(time.hour)) {
        val availableTrains = trainList.filter(t => t.capacity - t.numCurrentPeople >= group.size)
        if (availableTrains.nonEmpty) {
          val availableTrain = Some(availableTrains.head)
          availableTrain.get.enter(group, time)
          // Looking for the carriage in which the group has been placed
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
