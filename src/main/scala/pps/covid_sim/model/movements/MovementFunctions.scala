package pps.covid_sim.model.movements

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}

object MovementFunctions {

  // Note: use speed.delta to get the delta to update position

  def randomPath(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE): Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] = {
    coords => {
      // TODO
      ???
      //Coordinates.random(dimension)
    }
  }

  def linearPath(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE): Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] = {
    // TODO: implement something like wall following (see https://en.wikipedia.org/wiki/Maze_solving_algorithm) and/or
    //  https://link.springer.com/chapter/10.1007/978-3-319-62533-1_7
    coords => {
      // TODO
      ???
    }
  }

  def linearPathWithWallFollowing(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE): Set[Coordinates] => Set[Seq[Map[Group, Seq[Coordinates]]]] = {
    // TODO: implement something like wall following (see https://en.wikipedia.org/wiki/Maze_solving_algorithm) and/or
    //  https://link.springer.com/chapter/10.1007/978-3-319-62533-1_7
    coords => {
      // TODO
      ???
    }
  }

}
