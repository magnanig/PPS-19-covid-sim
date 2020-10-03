package pps.covid_sim.model.container

import pps.covid_sim.model.transports.PublicTransports.{BusLine, TrainLine}

// TODO scalaDoc
object TransportLinesContainer {

  private var _busLines: List[BusLine] = List()
  private var _trainLine: List[TrainLine] = List()

  def add(busLine: BusLine): Unit = {
    _busLines = busLine :: _busLines
  }

  def add(trainLine: TrainLine): Unit = {
    _trainLine = trainLine :: _trainLine
  }

  def getBusLines: List[BusLine] = _busLines

  def getTrainLines: List[TrainLine] = _trainLine

}
