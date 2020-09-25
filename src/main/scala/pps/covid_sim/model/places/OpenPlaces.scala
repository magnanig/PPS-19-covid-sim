package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.movements.MovementFunctions
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Rectangle.{generalObstacle, squareObstacle}
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle}
import pps.covid_sim.util.scheduling.TimeTable

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
    override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var umbrellas: Set[Rectangle] = Set()
      val umbrellasColumns = (dimension.width / 2).toInt
      val umbrellasRows = (dimension.length / 2).toInt

      (0 until umbrellasRows).foreach(r => (0 to umbrellasColumns).foreach(c => umbrellas += squareObstacle(r, c)))

      umbrellas
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles)
  }

  case class Square(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(50, 150),
      RandomGeneration.randomIntInRange(50, 150)
    )

    /**
     * Defines up to ten square obstacles, representing for example a fountain, a bench, etc...
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the place
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

  case class Park(override val city: City) extends OpenPlace {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(20, 80),
      RandomGeneration.randomIntInRange(20, 80)
    )

    /**
     * Defines park obstacles, representing trees.
     * @param dimension the dimension of the current space
     * @return          the set of obstacles of the park
     */
    override def placeObstacles(dimension: Dimension): Set[Rectangle] = {
      var trees: Set[Rectangle] = Set()
      val treesColumns = (dimension.width / 2).toInt
      val treesRows = (dimension.length / 2).toInt

      (0 until treesRows).foreach(r => (0 to treesColumns).foreach(c => trees += squareObstacle(r, c)))

      trees
    }

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles)
  }

  case class Field(override val city: City,
                   override val timeTable: TimeTable) extends OpenPlace with LimitedHourAccess {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(90, 120),
      RandomGeneration.randomIntInRange(45, 90)
    )

    override def placeObstacles(dimension: Dimension): Set[Rectangle] = Set.empty

    override val obstacles: Set[Rectangle] = placeObstacles(dimension)

    override val entranceCoords: Coordinates = Coordinates.randomOnBorder(dimension)

    override protected val pathSampling: Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] =
      MovementFunctions.randomPath(dimension, obstacles)
  }

}
