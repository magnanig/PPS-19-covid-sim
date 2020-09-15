package pps.covid_sim.view

import java.util.Calendar

import breeze.linalg.{DenseVector, linspace}
import breeze.plot.{Figure, plot}
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.SortedMap

case class LinePlot() {

  private val f = Figure("Covid-19 Simulation")
  private val p = f.subplot(0)
  private var x: DenseVector[Double] = DenseVector()
  private var from: Calendar = _

  f.width = 1000
  f.height = 700
  p.xlabel = "iteration"
  p.ylabel = "infections"

  def init(from: Calendar): Unit = { this.from = from }

  def drawLockdownThreshold(value: Double, upper: Boolean = false): Unit = {
    val color = if (upper) "black" else "g"
    p += plot(x, DenseVector.ones[Double](x.length) :*= value, style = '.', shapes = true, colorcode = color, lines = true)
  }

  def drawSimulation(values: SortedMap[Calendar, Int], avg: Boolean = false): Unit = {
    val data = DenseVector(values.values.map(_.toDouble).toArray)
    x = linspace(0, values.size, values.size)
    if (avg) p += plot(x, data, style = '-', shapes = true, colorcode = "red", lines = true, tips = data(_).toString)
    else p += plot(x, data)
  }

  def drawLockdownStart(time: Calendar, infections: Int): Unit = {
    drawPoint(time \ from, infections, "m")
  }

  def drawLockdownEnd(time: Calendar, infections: Int): Unit = {
    drawPoint(time \ from, infections, "g")
  }

  private def drawPoint(x: Int, y: Int, color: String): Unit = {
    p += plot(DenseVector(x), DenseVector(y), style = '-', colorcode = color,
      tips = _ => "(" + x + ", " + y.toString + ")", lines = true, shapes = true)
  }
}
