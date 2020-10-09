package pps.covid_sim.model.movement

import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.people.Person
import pps.covid_sim.util.geometry._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Object containing different movement functions used by the people inside the movement places.
 */
object MovementFunctions {

  private val samplesPerHour = 60
  private val obstacleCorners = 4

  /**
   * Movement function that simulates a random path, whose only purpose is to avoid obstacles within the place.
   * This function performs a path sampling, providing the coordinates of the path followed by each person
   * in the place in a given time slot.
   * @param dimension   the dimension of the place
   * @param obstacles   the obstacles within the place
   * @param speed       the speed of the people walking in the place
   * @param partitions  the partitioning of people
   * @return            a Set containing the sampling of the path for each person of each partition
   */
  def randomPath(dimension: Dimension,
                 obstacles: Set[Rectangle],
                 speed: Speed = Speed.MIDDLE,
                 partitions: Int): Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    groups => {
      def _nextPosition(leader: Person): Coordinates = {
        val nextPos: Coordinates = Coordinates.randomClose(dimension, leader.position, speed)
        if (obstacles.exists(r => nextPos.inside(r)) || nextPos.outOfDimension(dimension)) {
          leader.position
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

  /**
   * Movement function that simulates a linear path, whose purpose is to follow a quite regular path
   * following the obstacles inside the place.
   * This function performs a path sampling, providing the coordinates of the path followed by each person
   * in the place in a given time slot.
   * @param dimension   the dimension of the place
   * @param obstacles   the obstacles within the place
   * @param speed       the speed of the people walking in the place
   * @param partitions  the partitioning of people
   * @return            a Set containing the sampling of the path for each person of each partition
   */
  def linearPathWithWallFollowing(dimension: Dimension,
                                  obstacles: Set[Rectangle],
                                  speed: Speed = Speed.MIDDLE,
                                  partitions: Int): Set[Group] => Set[mutable.Seq[Map[Group, ArrayBuffer[Coordinates]]]] = {
    groups => {
      val obstaclesList = obstacles.toList
      var direction: Direction = Direction.randomDirection()
      var nextPos: Coordinates = (0, 0)
      var nextTarget: Option[Rectangle] = None
      var targetReached: Boolean = false
      var turns: Int = 0

      def _nextPosition(leader: Person): Coordinates = {
        if (!targetReached) {
          nextPos = Coordinates.directionClose(leader.position, speed, direction) // maintaining the previous direction
        } else { // target has been reached
          if (turns < obstacleCorners) {
            nextPos = Coordinates.followBorder(leader.position, speed, nextTarget.get)
            if (nextPos.onCorner(nextTarget.get)) turns += 1 // increase turns when a corner is reached
          } else {
            direction = if (leader.position == Coordinates(nextTarget.get.topLeftCorner.x,
                                                nextTarget.get.bottomRightCorner.y)) Direction.SOUTH_WEST
                        else Direction.NORTH_EAST
            nextPos = Coordinates.directionClose(leader.position, speed, direction)
            targetReached = false
            turns = 0
          }
        }

        if (nextPos.outOfDimension(dimension)) {
          direction = Direction.randomDirection()
          leader.position
        } else {
          if (obstaclesList.exists(r => nextPos.inside(r))) {
            obstaclesList.indices.foreach(i => if (nextPos.inside(obstaclesList(i))) nextTarget = Some(obstaclesList(i)))
            nextPos = Coordinates.translateOnBorder(nextPos, direction, nextTarget.get)
            targetReached = true
          }
          leader.position = nextPos
          leader.position
        }
      }

      val partitioning = generatePartitions(groups, partitions)
      partitioning.map(slot => slot.map(slotMember => slotMember.map(pair => {
        direction = Direction.randomDirection()
        targetReached = false
        nextTarget = None
        turns = 0
        (pair._1, (0 until samplesPerHour).map(_ => pair._2 += _nextPosition(pair._1.leader)))
      })))

      partitioning
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
