package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.rooms.{DiscoRoom, MultiRoom, TablesRoom}
import pps.covid_sim.parameters.CreationParameters.{maxNumOpenDiscoObstacles, maxNumPubObstacles, minNumOpenDiscoObstacles, minNumPubObstacles}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalIndoorObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}
import pps.covid_sim.util.scheduling.TimeTable

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FreeTime {

  trait FreeTimePlace[T <: Location] extends WorkPlace[T] with LimitedHourAccess

  case class Restaurant(override val city: City,
                        override val timeTable: TimeTable,
                        private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Restaurant] {
    val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = None
  }

  case class Bar(override val city: City,
                 override val timeTable: TimeTable,
                 private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Bar] {
    val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = None
  }

  case class Disco(override val city: City,
                   override val timeTable: TimeTable,
                   private var rooms: Seq[DiscoRoom] = Seq())
    extends MultiRoom[DiscoRoom](city, rooms) with FreeTimePlace[Disco] {
    val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = Some(Masks.Surgical)
  }

  case class Pub(override val city: City,
                 override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[Pub] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    /**
     * Defines the pub room's obstacles, representing for example the pub counter, the cash desk, etc...
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()
      val totObstacles = RandomGeneration.randomIntInRange(minNumPubObstacles, maxNumPubObstacles)
      @tailrec
      def _placeObstacles(): Unit = {
        val obstacle = generalIndoorObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        else obstacles += obstacle
      }

      (0 until totObstacles).foreach(_ => _placeObstacles())
      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 1)
  }

  case class OpenDisco(override val city: City,
                       override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[OpenDisco] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(30, 50),
      RandomGeneration.randomIntInRange(30, 50)
    )

    /**
     * Defines the open disco's obstacles, representing for example the bar counter, the cash desk, etc...
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()
      val totObstacles = RandomGeneration.randomIntInRange(minNumOpenDiscoObstacles, maxNumOpenDiscoObstacles)
      @tailrec
      def _placeObstacles(): Unit = {
        val obstacle = generalIndoorObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        else obstacles += obstacle
      }

      (0 until totObstacles).foreach(_ => _placeObstacles())
      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)
  }

}
