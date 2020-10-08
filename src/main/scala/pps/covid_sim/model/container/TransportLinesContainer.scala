package pps.covid_sim.model.container

import java.util.Calendar

import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.transports.PublicTransports._
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.parallel.ParSeq

object TransportLinesContainer {

  private var _busLines: ParSeq[BusLine] = ParSeq()
  private var _trainLine: ParSeq[TrainLine] = ParSeq()

  /**
   * Delete all transport lines created so far.
   */
  def reset(): Unit = {
    _busLines = ParSeq()
    _trainLine = ParSeq()
  }

  /**
   * Adds a new bus line in the container.
   */
  def add(busLine: BusLine): Unit = {
    _busLines = busLine +: _busLines
  }

  /**
   * Adds a new train line in the container.
   */
  def add(trainLine: TrainLine): Unit = {
    _trainLine = trainLine +: _trainLine
  }

  /**
   * Get all bus lines present in the entire
   * application domain
   *
   * @return  all bus lines present in the
   *          entire application domain
   */
  def getBusLines: ParSeq[BusLine] = _busLines

  /**
   * Get all train lines present in the entire
   * application domain
   *
   * @return  all train lines present in the
   *          entire application domain
   */
  def getTrainLines: ParSeq[TrainLine] = _trainLine

  /**
   * Get all transport bus lines that are available
   * from one city to another city at a given hour.
   *
   * @param from  departure city
   * @param to    city of arrival
   * @param date  availability date of the line
   * @return      all bus lines covering a specified
   *              route on the date indicated
   */
  def getBusLines(from: City, to: City, date: Calendar): List[Line[Bus]] = {
    getBusLines.filter(line => line.isReachable(from) && line.isReachable(to) && line.isOpen(date.hour)).toList
  }

  /**
   * Get all transport train lines that are available
   * from one city to another city at a given hour.
   *
   * @param from  departure city
   * @param to    city of arrival
   * @param date  availability date of the line
   * @return      all train lines covering a specified
   *              route on the date indicated
   */
  def getTrainLines(from: City, to: City, date: Calendar): List[Line[Train]] = {
    getTrainLines.filter(line => line.isReachable(from) && line.isReachable(to) && line.isOpen(date.hour)).toList
  }


  /**
   * Get all transport bus lines that are available
   * from the specified city at a given hour.
   *
   * @param in    departure city
   * @param date  availability date of the line
   * @return      all bus lines covering a specified
   *              route on the date indicated
   */
  def getBusLines(in: City, date: Calendar): List[Line[Bus]] = {
    getBusLines.filter(line => line.isReachable(in) && line.isOpen(date.hour)).toList
  }

  /**
   * Get all transport train lines that are available
   * from the specified city at a given hour.
   *
   * @param in    departure city
   * @param date  availability date of the line
   * @return      all train lines covering a specified
   *              route on the date indicated
   */
  def getTrainLines(in: City, date: Calendar): List[Line[Train]] = {
    getTrainLines.filter(line => line.isReachable(in) && line.isOpen(date.hour)).toList
  }

}
