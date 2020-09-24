package pps.covid_sim.model.places

import pps.covid_sim.model.clinical.Masks
import pps.covid_sim.model.clinical.Masks.Mask
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.places.Locations.Location
import pps.covid_sim.model.places.OpenPlaces.OpenPlace
import pps.covid_sim.model.places.rooms.{DiscoRoom, MultiRoom, TablesRoom}
import pps.covid_sim.util.RandomGeneration
import pps.covid_sim.util.geometry.Dimension
import pps.covid_sim.util.scheduling.TimeTable

object FreeTime {

  trait FreeTimePlace[T <: Location] extends WorkPlace[T] with LimitedHourAccess

  case class Restaurant(override val city: City,
                        override val timeTable: TimeTable,
                        private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Restaurant] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = None
  }

  case class Bar(override val city: City,
                 override val timeTable: TimeTable,
                 private var rooms: Seq[TablesRoom] = Seq())
    extends MultiRoom[TablesRoom](city, rooms) with FreeTimePlace[Bar] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = None
  }

  case class Disco(override val city: City,
                   override val timeTable: TimeTable,
                   private var rooms: Seq[DiscoRoom] = Seq())
    extends MultiRoom[DiscoRoom](city, rooms) with FreeTimePlace[Disco] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = Some(Masks.Surgical)
  }

  case class Pub(override val city: City,
                 override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[Pub] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(10, 20),
      RandomGeneration.randomIntInRange(10, 20)
    )

    override val mask: Option[Mask] = Some(Masks.Surgical)

  }

  case class OpenDisco(override val city: City,
                       override val timeTable: TimeTable) extends OpenPlace with FreeTimePlace[OpenDisco] {
    override val dimension: Dimension = (
      RandomGeneration.randomIntInRange(30, 50),
      RandomGeneration.randomIntInRange(30, 50)
    )

    override val mask: Option[Mask] = Some(Masks.Surgical)

  }

}
