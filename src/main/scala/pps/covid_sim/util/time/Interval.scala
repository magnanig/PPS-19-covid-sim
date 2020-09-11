package pps.covid_sim.util.time

import java.util.Calendar

import pps.covid_sim.util.time.Time.ScalaCalendar

/**
 * Represents an interval, from a starting moment to a final one.
 * @param from                      the starting moment (inclusive)
 * @param to                        the final moment (inclusive)
 * @param nextMoment                a function that, given a moment, specifies how to get the next one
 * @param datesIntervalToInterval   a function that converts a dates interval into some interval
 * @tparam A                        the type of moment
 */
abstract class Interval[A](val from: A,
                           val to: A,
                           private val nextMoment: A => A,
                           private val datesIntervalToInterval: DatesInterval => Interval[A])
                          (implicit ordering: A => Ordered[A]) extends Iterable[A] {
  /**
   * Check if the specified moment is inside the interval.
   * @param moment  the moment to be checked
   * @return        true if the specified moment is inside the interval, false otherwise
   */
  def contains(moment: A): Boolean = moment.compareTo(from) >= 0 && moment.compareTo(to) <= 0

  /**
   * Check if the specified interval overlaps the current one.
   * @param interval  the interval to be checked
   * @return          true if the specified interval overlaps the current one, false otherwise
   */
  def overlaps(interval: Interval[A]): Boolean = from.compareTo(interval.to) <= 0 && interval.from.compareTo(to) <= 0

  /**
   * Check if the specified dates interval overlaps the current interval.
   * @param datesInterval   the dates interval to be checked
   * @return                true if the specified dates interval overlaps the current interval, false otherwise
   */
  def overlapsPeriod(datesInterval: DatesInterval): Boolean = datesIntervalToInterval(datesInterval) match {
    case Interval(from, until) if from == until => contains(from)
    case interval: Interval[A] => overlaps(interval)
  }

  override def iterator: Iterator[A] = new Iterator[A] {

    private var current = from
    private var ended = false

    override def next(): A = {
      if (!hasNext) {
        throw new NoSuchElementException()
      }
      val ret = current
      current = nextMoment(current)
      ended = ret == Interval.this.to
      ret
    }

    override def hasNext: Boolean = !ended
  }
}

object Interval {
  implicit def calendarToInterval(calendar: Calendar): Interval[Calendar] = DatesInterval(calendar, calendar + 1)

  def unapply[A](arg: Interval[A]): Option[(A, A)] = Some(arg.from, arg.nextMoment(arg.to))
}
