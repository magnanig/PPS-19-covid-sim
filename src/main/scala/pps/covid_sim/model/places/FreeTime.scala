package pps.covid_sim.model.places

import java.util.Calendar

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movement.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.arranging.Placement.Placeholder
import pps.covid_sim.model.places.rooms.{DiscoRoom, MultiRoom, Room, TablesRoom}
import pps.covid_sim.model.scheduling.TimeTable
import pps.covid_sim.parameters.CreationParameters.{maxNumOpenDiscoObstacles, maxNumPubObstacles, minNumOpenDiscoObstacles, minNumPubObstacles}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FreeTime {

  trait FreeTimePlace[T <: Location] extends WorkPlace[T] with LimitedHourAccess


  case class Restaurant(override val city: City,
                        override val timeTable: TimeTable,
                        override val openedInLockdown: Boolean,
                        private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Restaurant] {

    override val mask: Option[Mask] = None
  }


  case class Bar(override val city: City,
                 override val timeTable: TimeTable,
                 override val openedInLockdown: Boolean,
                 private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Bar] {

    override val mask: Option[Mask] = None
  }


  case class Disco(override val city: City,
                   override val timeTable: TimeTable,
                   override val openedInLockdown: Boolean,
                   private var rooms: Seq[DiscoRoom] = Seq())
    extends MultiRoom[DiscoRoom](city, rooms) with FreeTimePlace[Disco] {

    override protected[places] def freeRoom(group: Group, time: Calendar): Option[(Room, Seq[Placeholder[Group]])] = {
      rooms.find(_.canEnter(group, time)) match {
        case Some(room) =>  Some((room, Seq.empty))
        case _ => None
      }
    }

    override val mask: Option[Mask] = Some(Masks.Surgical)
  }


  case class Pub(override val city: City,
                 override val openedInLockdown: Boolean,
                 override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[Pub] {

    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension, minNumPubObstacles, maxNumPubObstacles)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 1)
  }


  case class OpenDisco(override val city: City,
                       override val timeTable: TimeTable,
                       override val openedInLockdown: Boolean) extends OpenPlace with FreeTimePlace[OpenDisco] {

    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(30, 50),
      RandomGeneration.randomIntInRange(30, 50)
    )

    override val obstacles: Set[Rectangle] = Obstacles.placeObstacles(dimension, minNumOpenDiscoObstacles,
      maxNumOpenDiscoObstacles)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)
  }

}
