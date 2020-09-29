package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.parameters.CreationParameters.{beachFillFactor, maxParkObstaclesFactor, maxSquareObstaclesFactor, minParkObstaclesFactor, minSquareObstaclesFactor}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.{beachObstacle, calculateFilling, generalOutdoorObstacle}
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}
import pps.covid_sim.util.scheduling.TimeTable

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object OpenPlaces {

  trait OpenPlace extends Place with MovementSpace {
    //override lazy val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    // must be lazy since dimension will be defined after this trait initialization
    /*
    protected override lazy val movement: (Coordinates, Set[Person]) => Coordinates =
      MovementFunctions.randomPath(dimension, Set.empty)
    */

    override def mask: Option[Mask] = if(dimension.surface / numCurrentPeople < 0) //Parameters.safeSurfacePerPerson) TODO
      Some(Masks.Surgical) else None
  }

  case class Beach(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 200),
      RandomGeneration.randomIntInRange(50, 80)
    )

    /**
     * Defines beach obstacles, representing umbrellas.
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the beach
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var umbrellas: Set[Rectangle] = Set()
      val umbrellasColumns = calculateFilling(dimension.width, beachFillFactor)
      val umbrellasRows = calculateFilling(dimension.length, beachFillFactor)

      (0 until umbrellasRows).foreach(r => (0 until umbrellasColumns)
        .foreach(c => umbrellas += beachObstacle(r, c, beachFillFactor)))

      umbrellas
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.SLOW, 6)
  }

  case class Square(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 150),
      RandomGeneration.randomIntInRange(50, 150)
    )

    /**
     * Defines the square obstacles (e.g. benches, fountains, etc...).
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the square
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()
      val minObstacles: Int = (dimension.surface / minSquareObstaclesFactor).toInt
      val maxObstacles: Int = (dimension.surface / maxSquareObstaclesFactor).toInt
      val totObstacles = RandomGeneration.randomIntInRange(minObstacles, maxObstacles)
      @tailrec
      def _placeObstacles(): Unit = {
        val obstacle = generalOutdoorObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        else obstacles += obstacle
      }

      (0 until totObstacles).foreach(_ => _placeObstacles())
      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val mask: Option[Mask] = Some(Masks.Surgical)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 4)
  }

  case class Park(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(20, 80),
      RandomGeneration.randomIntInRange(20, 80)
    )

    /**
     * Defines the park obstacles (e.g. trees, benches, fountains, etc...).
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the park
     */
    private def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var obstacles: Set[Rectangle] = Set()
      val minObstacles: Int = (dimension.surface / minParkObstaclesFactor).toInt
      val maxObstacles: Int = (dimension.surface / maxParkObstaclesFactor).toInt
      val totObstacles = RandomGeneration.randomIntInRange(minObstacles, maxObstacles)
      @tailrec
      def _placeObstacles(): Unit = {
        val obstacle = generalOutdoorObstacle(dimension)
        if (obstacles.exists(r => r.vertexes.exists(c => c.inside(obstacle)))) _placeObstacles()
        else obstacles += obstacle
      }

      (0 until totObstacles).foreach(_ => _placeObstacles())
      obstacles
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.MIDDLE, 3)
  }

  case class Field(override val city: City,
                   override val timeTable: TimeTable) extends OpenPlace with LimitedHourAccess {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(90, 120),
      RandomGeneration.randomIntInRange(45, 90)
    )

    private def placeObstacles(dimension: Dimension): Set[Rectangle] = Set.empty

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override protected val pathSampling: Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles, Speed.FAST, 1)
  }

}
