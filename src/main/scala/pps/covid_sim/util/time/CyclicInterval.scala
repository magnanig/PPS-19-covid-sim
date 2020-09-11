package pps.covid_sim.util.time

/**
 *
 * @param from                      the starting moment (inclusive)
 * @param to                        the final moment (inclusive)
 * @param toInt                     a function that converts a moment into a number
 * @param nextMoment                a function that, given a moment, specifies how to get the next one
 * @param datesIntervalToInterval   a function that converts a dates interval into some interval
 * @tparam A                        the type of moment
 */
abstract class CyclicInterval[A](override val from: A,
                                 override val to: A,
                                 protected val toInt: A => Int,
                                 private val nextMoment: A => A,
                                 private val datesIntervalToInterval: DatesInterval => Interval[A])
                                (implicit ordering: A => Ordered[A])
  extends Interval[A](from, to, nextMoment, datesIntervalToInterval) {

  protected val cycleLength: Int

  override def contains(moment: A): Boolean = {
    if(to > from) toInt(moment) >= toInt(from) && toInt(moment) <= toInt(to)
    else          toInt(moment) >= toInt(from) || toInt(moment) <= toInt(to)
  }

  override def overlaps(interval: Interval[A]): Boolean = overlap(toInt(from), linearizeTo(), toInt(interval.from),
    linearizeTo(interval))

  private def linearizeTo(cyclicInterval: Interval[A] = this): Int = {
    if (toInt(cyclicInterval.from) > toInt(cyclicInterval.to)) toInt(cyclicInterval.to) + cycleLength
    else toInt(cyclicInterval.to)
  }

  private def overlap(a0: Int, a1: Int, b0: Int, b1: Int): Boolean = {
    if (a1 >= b0 && a0 <= b1) return true

    if (a1 + cycleLength >= b0 && a0 + cycleLength <= b1) return true
    if (a1 - cycleLength >= b0 && a0 - cycleLength <= b1) return true

    false
  }
}
