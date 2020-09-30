package pps.covid_sim.model.movements

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.util.geometry.Coordinates.randomClose
import pps.covid_sim.util.geometry.{Coordinates, Dimension, Rectangle, Speed}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object MovementFunctions {

  val samplesPerHour = 60

  /**
   * Movement function that simulates a random path, whose only purpose is to avoid obstacles within the place.
   * This function performs a path sampling, providing the coordinates of the path followed by each person
   * in the place in a given time slot.
   * @param dimension   the dimension of the place
   * @param obstacles   the obstacles within the place
   * @param speed       the speed of the people walking in the place
   * @param partitions  the partitioning of people
   * @return            a Set
   */
  def randomPath(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE,
                 partitions: Int): Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    groups => {
      @tailrec
      def _nextPosition(leader: Person): Coordinates = {
        val nextPos: Coordinates = randomClose(dimension, leader.position, speed)
        if (obstacles.exists(r => nextPos.inside(r)) || nextPos.outOfDimension(dimension)) {
          _nextPosition(leader)
        } else {
          leader.position = nextPos
          leader.position
        }
      }

      val partitioning = generatePartitions(groups, partitions)
      partitioning.map(slot => slot.map(slotMember => slotMember.map(pair =>
        (pair._1, (0 until samplesPerHour).map(_ => pair._2 += _nextPosition(pair._1.leader))))))

      partitioning
    }

  }

  def linearPath(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE,
                 partitions: Int): Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    // TODO: implement something like wall following (see https://en.wikipedia.org/wiki/Maze_solving_algorithm) and/or
    //  https://link.springer.com/chapter/10.1007/978-3-319-62533-1_7
    groups => {
      // TODO
      ???
    }
  }

  def linearPathWithWallFollowing(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE): Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    // TODO: implement something like wall following (see https://en.wikipedia.org/wiki/Maze_solving_algorithm) and/or
    //  https://link.springer.com/chapter/10.1007/978-3-319-62533-1_7
    groups => {
      // TODO
      ???
    }
  }

  /**
   * This method generates a partition of the groups within the place.
   * Only the people inside a specific partition are present at the same time in the place: this simulates the fact
   * that people do not arrive at the place at the same time, therefore they are not all present at the same time.
   * @param groups      the groups in the place in a given time interval
   * @param partitions  the number of partitions to be performed, in order to manage different subgroups of people,
   *                    arriving at the place at different times, within the given time interval
   * @return            a Set containing the partitions
   */
  private def generatePartitions(groups: Set[Group],
                                 partitions: Int): Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    val groupsList = groups.toList
    var slots: ArrayBuffer[ArrayBuffer[Map[Group, ArrayBuffer[Coordinates]]]] = ArrayBuffer()
    (0 until partitions).foreach(_ => slots += ArrayBuffer())

    groupsList.indices.map(i => slots(i % partitions)
      += Map(groupsList(i) -> ArrayBuffer(groupsList(i).leader.position))).toSet
  }

}
