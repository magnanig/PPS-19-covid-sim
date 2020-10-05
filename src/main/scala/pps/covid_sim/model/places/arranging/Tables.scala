package pps.covid_sim.model.places.arranging

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.clinical.VirusPropagation
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Place
import pps.covid_sim.model.places.arranging.Placement.{ItemArrangement, ItemGroup, Row}
import pps.covid_sim.util.RandomGeneration

object Tables {

  /**
   * An arrangement of tables, useful in bars or restaurants.
   * @param numRows                 the desired number of rows in disposition
   * @param numPlacesInTablesGroup  the number of places per column tables group. For example, (2, 4, 6)
   *                                means that each row is composed of 3 columns, where the first one
   *                                has got a tables group that can contains up to 2 people, the second
   *                                one at most 4 and the third at most 6.
   */
  case class TablesArrangement(numRows: Int, numPlacesInTablesGroup: Int*)
    extends ItemArrangement[TablesGroup](numRows, _ => TablesRow(numPlacesInTablesGroup.map(TablesGroup))) {
    require(numPlacesInTablesGroup.nonEmpty)

    override val totalCapacity: Int = rows.size * numPlacesInTablesGroup.sum

    override def getItemGroup(group: Group): Option[TablesGroup] = rows
      .flatMap(_.itemGroups)
      .collectFirst({ case t: TablesGroup if t.assignee.contains(group) => t })

    override def iterator: Iterator[TablesGroup] = rows.flatMap(_.itemGroups).iterator
  }

  object TablesArrangement {
    /**
     * Generate a random tables arrangement, that can containt at least the specified number
     * of people and each tables group has the desired max capacity.
     * @param numPerson               the desired minimum number of people to be contained in
     *                                the arrangement
     * @param maxTableGroupCapacity   the desired max tables group capacity
     * @return                        a random tables arrangement respecting the above constraints
     */
    def randomFor(numPerson: Int, maxTableGroupCapacity: Int): TablesArrangement = {
      val columns = RandomGeneration.randomIntInRange(2, 10)
      val numPlacesInEachGroup = (1 to columns).map(_ => RandomGeneration.randomIntInRange(2, maxTableGroupCapacity))
      val rows = Math.ceil(numPerson.toDouble / numPlacesInEachGroup.sum).toInt
      TablesArrangement(rows, numPlacesInEachGroup:_*)
    }
  }


  /**
   * A row of tables group.
   * @param itemGroups    the sequence of tables group in the current row
   */
  case class TablesRow(override val itemGroups: Seq[TablesGroup]) extends Row[TablesGroup]


  /**
   * A single tables group, with the specified max capacity.
   * @param capacity    the max people capacity in current tables group
   */
  case class TablesGroup(override val capacity: Int) extends ItemGroup {
    private[places] var assignee: Option[Group] = None

    override def isFree: Boolean = assignee.isEmpty

    /**
     * Assign current tables group to the specified group.
     * @param group   the group to be assigned
     * @return        true if group has been successfully assigned to current tables group,
     *                false otherwise
     */
    def assign(group: Group): Boolean = assignee match {
      case None => assignee = Some(group); true
      case _ => false
    }

    override def release(): Unit = { assignee = None }

    override def propagateVirus(place: Place, time: Calendar)(covidInfectionParameters: CovidInfectionParameters): Unit = {
      assignee match {
        case Some(group) => (group.last :: group.toList)
          .sliding(2)
          .foreach(pair => VirusPropagation(covidInfectionParameters).tryInfect(pair.head, pair.last, place, time))
        case None =>
      }
    }
  }

}
