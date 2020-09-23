package pps.covid_sim.util.geometry

object MovementFunctions {

  // Note: use speed.delta to get the delta to update position

  def randomPath(dimension: Dimension,
                 speed: Speed = Speed.MIDDLE): Coordinates => Coordinates = {
    coords => {
      // TODO
      Coordinates.random(dimension)
    }
  }

  def linearPath(dimension: Dimension,
                 speed: Speed = Speed.MIDDLE): Coordinates => Coordinates = {
    // TODO: implement something like wall following (see https://en.wikipedia.org/wiki/Maze_solving_algorithm) and/or
    //  https://link.springer.com/chapter/10.1007/978-3-319-62533-1_7
    coords => {
      // TODO
      Coordinates.random(dimension)
    }
  }

}
