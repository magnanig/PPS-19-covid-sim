package pps.covid_sim.model.container

import pps.covid_sim.model.transports.PublicTransports.{BusLine, TrainLine}

object TransportLinesContainer {

  private var _busLines: List[BusLine] = List()
  private var _trainLine: List[TrainLine] = List()

  /**
   * Adds a new bus line in the container.
   */
  def add(busLine: BusLine): Unit = {
    _busLines = busLine :: _busLines
  }

  /**
   * Adds a new train line in the container.
   */
  def add(trainLine: TrainLine): Unit = {
    _trainLine = trainLine :: _trainLine
  }

  /**
   * Get all bus lines present in the entire
   * application domain
   *
   * @return  all bus lines present in the
   *          entire application domain
   */
  def getBusLines: List[BusLine] = _busLines

  /**
   * Get all train lines present in the entire
   * application domain
   *
   * @return  all train lines present in the
   *          entire application domain
   */
  def getTrainLines: List[TrainLine] = _trainLine

}
