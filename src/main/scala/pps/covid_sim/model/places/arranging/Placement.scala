package pps.covid_sim.model.places.arranging

import java.util.Calendar

import pps.covid_sim.model.CovidInfectionParameters
import pps.covid_sim.model.people.PeopleGroup.Group
import pps.covid_sim.model.places.Locations.Location

object Placement {

  /**
   * Represents a place where group can be assigned to (e.g. a school desk,
   * group of tables in a bar, ecc).
   * @tparam A  the type of the placeholder assignee
   */
  trait Placeholder[A] extends Ordered[Placeholder[A]] {

    /**
     * The max capacity.
     */
    val capacity: Int

    /**
     * Checks whether current placeholder is free or not.
     * @return  true if current placeholder is free, false otherwise
     */
    def isFree: Boolean

    /**
     * Assign current placeholder to the specified assignee.
     * @param assignee  the assignee of current placeholder
     * @return
     */
    def assign(assignee: A): Boolean

    /**
     * Releases current placeholder.
     */
    def release(): Unit

    override def compare(that: Placeholder[A]): Int = this.capacity.compareTo(that.capacity)
  }

  /**
   * Represents the arrangement (i.e. disposition) of some item groups inside a location.
   * @tparam T  the type of item groups to be arranged
   */
  trait Arrangement[T <: ItemGroup] {

    /**
     * The total capacity, by summing all item groups capacity.
     */
    val totalCapacity: Int

    /**
     * The sequence of rows in the arrangement.
     */
    val rows: Seq[Row[T]]

    /**
     * Get the item group associated to the specified group, if any.
     * @param group   the group to be found in some item group
     * @return        the optional item group associated to the specified group
     */
    def getItemGroup(group: Group): Option[T]
  }


  /**
   * Represents a row in the arrangement, composed of many item groups.
   * @tparam T    the type of each item group
   */
  trait Row[T <: ItemGroup] {
    val itemGroups: Seq[T]
  }

  /**
   * The item group, where groups can be assigned to.
   */
  trait ItemGroup extends Placeholder[Group] {
    /**
     * Propagates virus inside current item group.
     * @param place   the place where infection takes place
     * @param time    the time when infection happens
     */
    def propagateVirus(place: Location, time: Calendar)(covidInfectionParameters: CovidInfectionParameters): Unit
  }


  /**
   * A partial implementation of a generic items arrangement.
   * @param numRows     the desired number of rows in disposition
   * @param rowMapping  a function that maps each row number to a concrete Row implementation
   * @tparam T          the type of item groups to be arranged
   */
  abstract class ItemArrangement[T <: ItemGroup](numRows: Int,
                                                 rowMapping: Int => Row[T]) extends Arrangement[T] with Iterable[T] {
    override val rows: Seq[Row[T]] = (1 to numRows).map(rowMapping)
  }

}
