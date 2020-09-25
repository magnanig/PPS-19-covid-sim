package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.rooms.{DiscoRoom, MultiRoom, TablesRoom}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.generalObstacle
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}
import pps.covid_sim.util.scheduling.TimeTable

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
     * Defines up to three pub room's obstacles, representing for example the pub counter, the cash desk, etc...
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()

      def _placeObstacles(): Unit = {
        val obstacle = generalObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        obstacles += obstacle
      }
      (0 until RandomGeneration.randomIntInRange(0, 3)).foreach(_ => _placeObstacles())

      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles)
  }

  case class OpenDisco(override val city: City,
                       override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[OpenDisco] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(30, 50),
      RandomGeneration.randomIntInRange(30, 50)
    )

    /**
     * Defines up to ten open disco's obstacles, representing for example the bar counter, the cash desk, etc...
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the room
     */
    override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()

      def _placeObstacles(): Unit = {
        val obstacle = generalObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        obstacles += obstacle
      }
      (0 until RandomGeneration.randomIntInRange(0, 9)).foreach(_ => _placeObstacles())

      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles)
  }

}
