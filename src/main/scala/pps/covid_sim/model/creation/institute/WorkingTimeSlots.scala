package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.places.Education.Classroom
import pps.covid_sim.util.time.DaysInterval
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.Time.Day.Day

/**
 * Represents all the lesson time slots of all classrooms.
 *
 * @param rooms         classrooms where lessons are held.
 * @param daysInterval  days of lessons.
 * @param fromHour      start time of lessons.
 * @param toHour        end time of lessons.
 */
case class WorkingTimeSlots(rooms: List[Classroom],
                            daysInterval: DaysInterval = DaysInterval(Day.MONDAY, Day.SATURDAY),
                            fromHour: Int = 8,
                            toHour: Int = 13) extends Iterable[(Classroom, Day, Int)] {

  /**
   * It is used to obtain all room, day and time
   * combinations within the school calendar.
   * So you can get all the lesson time slots
   * in all the rooms of the school. For example,
   * each slot obtained can be assigned to a professor.
   *
   * @return an iterator over each possible pair
   *         of classes, days, and hours.
   */
  override def iterator: Iterator[(Classroom, Day, Int)] = {
    (for(room <- rooms; day <- daysInterval; hour <- (fromHour until toHour).toList)
      yield (room, day, hour)).iterator
  }
}