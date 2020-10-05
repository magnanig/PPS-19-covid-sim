package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.places.Education.Classroom
import pps.covid_sim.util.time.DaysInterval
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.Time.Day.Day

case class WorkingTimeSlots(rooms: List[Classroom],
                            daysInterval: DaysInterval = DaysInterval(Day.MONDAY, Day.SATURDAY),
                            fromHour: Int = 8,
                            toHour: Int = 13) extends Iterable[(Classroom, Day, Int)]{

  override def iterator: Iterator[(Classroom, Day, Int)] = {
    (for(_ <- 1 until 5 * 6; room <- rooms; day <- daysInterval; hour <- (fromHour until toHour).toList)
      yield (room, day, hour)).iterator
  }
}