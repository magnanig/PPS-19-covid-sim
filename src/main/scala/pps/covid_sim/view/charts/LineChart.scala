package pps.covid_sim.view.charts

import java.awt.{BasicStroke, Color}
import java.util.Calendar

import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.block.BlockBorder
import org.jfree.chart.plot.{PlotOrientation, XYPlot}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.title.TextTitle
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.SortedMap
import scala.swing.Font

/**
 * Class that manages the creation of a line chart.
 *
 * @param title      the title of the chart
 * @param xAxisLabel the label of the x-axis
 * @param yAxisLabel the label of the y-axis
 * @param legend     the series legend
 */
case class LineChart(title: String,
                     from: Calendar,
                     xAxisLabel: String,
                     yAxisLabel: String,
                     legend: String) {

  private var chart: JFreeChart = _
  private var plot: XYPlot = _
  private val dataset: XYSeriesCollection = new XYSeriesCollection()
  private val startLockdownSeries: XYSeries = new XYSeries("Start Lockdown")
  private val endLockdownSeries: XYSeries = new XYSeries("End Lockdown")
  private var seriesCount = 0

  dataset.addSeries(startLockdownSeries)
  dataset.addSeries(endLockdownSeries)

  /**
   * Method that draws a line chart representing the evolution of a phenomenon over time (e.g. infections trends).
   *
   * @param infected a map containing the number of people (map value) affected by the phenomenon
   *                 for each day (map key)
   * @param avg      true if the line chart refers to an average of multiple runs
   * @return a ChartPanel containing the line chart
   */
  def drawChart(infected: SortedMap[Calendar, Int], avg: Boolean = false): ChartPanel = {
    seriesCount = seriesCount + 1
    val seriesName = if (!avg) legend + " " + seriesCount else "AVG"
    val mainSeries = new XYSeries(seriesName)

    infected.zipWithIndex.foreach(elem => mainSeries.add(elem._2, elem._1._2))
    dataset.addSeries(mainSeries)

    chart = ChartFactory.createXYLineChart(
      title,
      xAxisLabel,
      yAxisLabel,
      dataset,
      PlotOrientation.VERTICAL,
      true, true, false)

    plot = chart.getXYPlot

    val xAxis = new NumberAxis()
    xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    plot.setDomainAxis(xAxis)

    val renderer = new XYLineAndShapeRenderer()

    renderer.setSeriesPaint(dataset.getSeriesIndex("Start Lockdown"), Color.BLACK)
    renderer.setSeriesLinesVisible(dataset.getSeriesIndex("Start Lockdown"), false)

    renderer.setSeriesPaint(dataset.getSeriesIndex("End Lockdown"), Color.GREEN)
    renderer.setSeriesLinesVisible(dataset.getSeriesIndex("End Lockdown"), false)

    if (avg) {
      renderer.setSeriesStroke(0, new BasicStroke(2.0f))
      renderer.setSeriesPaint(dataset.getSeriesIndex(seriesName), Color.BLACK)
    } else {
      renderer.setSeriesPaint(dataset.getSeriesIndex(seriesName), Color.RED)
    }

    renderer.setSeriesLinesVisible(dataset.getSeriesIndex(seriesName), true)

    plot.setRenderer(renderer)
    plot.setBackgroundPaint(Color.white)

    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(Color.BLACK)

    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(Color.BLACK)

    chart.getLegend().setFrame(BlockBorder.NONE)
    chart.setTitle(new TextTitle(title, Font("Sans Serif", Font.Bold, 18)))

    new ChartPanel(chart)
  }

  /**
   * Method that draws a line chart with multiple series representing the evolution of a phenomenon over time
   * (e.g. infections trends).
   *
   * @param infectedPerStage a map containing the number of people affected by the phenomenon
   *                         for each day (map value) for each stage (map key)
   * @return a ChartPanel containing the line chart
   */
  def drawMultiSeriesChart(infectedPerStage: Map[Int, SortedMap[Calendar, Int]]): ChartPanel = {
    var stageSeries: Seq[XYSeries] = Seq()

    infectedPerStage.foreach(elem => {
      val series = new XYSeries(s"Stage ${elem._1}")
      elem._2.zipWithIndex.foreach(pair => series.add(pair._2, pair._1._2))
      stageSeries = stageSeries :+ series
    })

    stageSeries.foreach(series => dataset.addSeries(series))

    chart = ChartFactory.createXYLineChart(
      title,
      xAxisLabel,
      yAxisLabel,
      dataset,
      PlotOrientation.VERTICAL,
      true, true, false)

    plot = chart.getXYPlot

    val xAxis = new NumberAxis()
    xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    plot.setDomainAxis(xAxis)

    val renderer = new XYLineAndShapeRenderer()

    plot.setRenderer(renderer)
    plot.setBackgroundPaint(Color.white)

    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(Color.BLACK)

    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(Color.BLACK)

    chart.getLegend().setFrame(BlockBorder.NONE)
    chart.setTitle(new TextTitle(title, Font("Sans Serif", Font.Bold, 18)))

    new ChartPanel(chart)
  }

  /**
   * Method that adds to the specific series the start of a lockdown.
   *
   * @param time       when the lockdown starts
   * @param infections the number of infections at that time
   */
  def drawLockDownStart(time: Calendar, infections: Int): Unit = {
    startLockdownSeries.add(time \ from, infections)
  }

  /**
   * Method that adds to the specific series the end of a lockdown.
   *
   * @param time       when the lockdown ends
   * @param infections the number of infections at that time
   */
  def drawLockDownEnd(time: Calendar, infections: Int): Unit = {
    endLockdownSeries.add(time \ from, infections)
  }

}
