package pps.covid_sim.model.creation.institute

import pps.covid_sim.model.places.Education.Classroom
import pps.covid_sim.util.time.DaysInterval
import pps.covid_sim.util.time.Time.Day
import pps.covid_sim.util.time.Time.Day.Day

private[institute] case class WorkingTimeSlots(rooms: List[Classroom],
                            daysInterval: DaysInterval = DaysInterval(Day.MONDAY, Day.SATURDAY),
                            fromHour: Int = 8,
                            toHour: Int = 13) {

  private var index: Int = 0
  private val slots = for(room <- rooms; day <- daysInterval; hour <- (fromHour until toHour).toList)
    yield (room, day, hour)

  def hasNext: Boolean = index < (rooms.size * 5 * 6)

  def next: (Classroom, Day, Int) = {
    val _return = slots(index)
    index += 1
    _return
  }

}