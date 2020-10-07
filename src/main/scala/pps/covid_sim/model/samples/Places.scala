package pps.covid_sim.model.samples

import pps.covid_sim.model.places.FreeTime.Bar
import pps.covid_sim.model.places.OpenPlaces.Park
import pps.covid_sim.model.places.rooms.TablesRoom
import pps.covid_sim.util.scheduling.TimeTable
import pps.covid_sim.util.time.Time.{Day, Month}
import pps.covid_sim.util.time.TimeIntervalsImplicits._
import pps.covid_sim.util.time.{DaysInterval, HoursInterval}

object Places {

  val SCHOOL_TIME_TABLE: TimeTable = TimeTable(Month.SEPTEMBER -> Month.JUNE)
    .add(Day.MONDAY -> Day.SATURDAY, 8 -> 15)
  val UNIVERSITY_TIME_TABLE: TimeTable = TimeTable(Month.SEPTEMBER -> Month.JULY)
    .add(Day.MONDAY -> Day.FRIDAY, 8 -> 18)

  val SHOP_TIME_TABLE: TimeTable = TimeTable().add(Day.TUESDAY -> Day.SUNDAY, 9 -> 13, 15 -> 19)
  val FOOTBALL_FIELD_PRIVATE_TIME_TABLE: TimeTable = TimeTable().add(Day.TUESDAY -> Day.FRIDAY, 7 -> 17)
  val FOOTBALL_FIELD_PUBLIC_TIME_TABLE: TimeTable = TimeTable().add(Day.MONDAY -> Day.SUNDAY, 9 -> 22)
  val GYM_TIME_TABLE: TimeTable = TimeTable().add(Day.MONDAY -> Day.SATURDAY, 9 -> 20)

  val RESTAURANT_TIME_TABLE: TimeTable = TimeTable().add(Day.TUESDAY -> Day.SUNDAY, 11 -> 14, 19 -> 22)
  val BAR_TIME_TABLE: TimeTable = TimeTable().add(Day.TUESDAY -> Day.SUNDAY, 6 -> 18)
  val PUB_TIME_TABLE: TimeTable = TimeTable().add(Day.THURSDAY -> Day.SUNDAY, 18 -> 2)
  val DISCO_TIME_TABLE: TimeTable = TimeTable().add(Day.FRIDAY -> Day.SATURDAY, 22 -> 6)

  val SMALL_ROOM: TablesRoom = TablesRoom(2, 4)
  val BIG_ROOM: TablesRoom = TablesRoom(3, 8)

  val BAR: Bar = Bar(
    Cities.CERVIA,
    TimeTable()
      .add(Day.MONDAY, HoursInterval(8, 12))
      .add(DaysInterval(Day.TUESDAY, Day.FRIDAY), HoursInterval(8, 12), HoursInterval(14, 18))
      .add(DaysInterval.WEEKEND, HoursInterval(9, 20)),
    false,
    List(SMALL_ROOM, BIG_ROOM)
  )

  val PARK: Park = Park(Cities.CERVIA, false)

}
