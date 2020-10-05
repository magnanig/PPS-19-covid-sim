package pps.covid_sim.view

import java.awt.{BasicStroke, Color}
import java.io.File
import java.nio.file.{Files, Paths}
import java.util.{Calendar, Date}

import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.block.BlockBorder
import org.jfree.chart.plot.{PlotOrientation, XYPlot}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.title.TextTitle
import org.jfree.chart.{ChartFactory, ChartPanel, ChartUtils, JFreeChart}
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.collection.SortedMap
import scala.swing.Font

/**
 * Class that manages the creation of a line chart.
 * @param title         the title of the chart
 * @param xAxisLabel    the label of the x-axis
 * @param yAxisLabel    the label of the y-axis
 * @param legend        the series legend
 */
case class LineChart(title: String,
                     from: Calendar,
                     xAxisLabel: String,
                     yAxisLabel: String,
                     legend: String) {

  private var chart: JFreeChart = _
  private var plot: XYPlot = _
  private val dataset: XYSeriesCollection = new XYSeriesCollection()
  private val mainSeries: XYSeries = new XYSeries(legend)
  private val startLockdownSeries: XYSeries = new XYSeries("Start Lockdown")
  private val endLockdownSeries: XYSeries = new XYSeries("End Lockdown")

  dataset.addSeries(startLockdownSeries)
  dataset.addSeries(endLockdownSeries)

  /**
   * Method that draws a line chart representing the evolution of a phenomenon over time (e.g. infections trends).
   * @param infected      a map containing the number of people (map value) affected by the phenomenon
   *                      for each day (map key)
   * @param avg           true if the line chart refers to an average of multiple runs
   * @return              a ChartPanel containing the line chart
   */
  def drawChart(infected: SortedMap[Calendar, Int], avg: Boolean = false): ChartPanel = {
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
      renderer.setSeriesPaint(dataset.getSeriesIndex(legend), Color.BLACK)
    } else {
      renderer.setSeriesPaint(dataset.getSeriesIndex(legend), Color.RED)
    }

    renderer.setSeriesLinesVisible(dataset.getSeriesIndex(legend), true)

    plot.setRenderer(renderer)
    plot.setBackgroundPaint(Color.white)

    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(Color.BLACK)

    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(Color.BLACK)

    chart.getLegend().setFrame(BlockBorder.NONE)
    chart.setTitle(new TextTitle(title, Font("Sans Serif", Font.Bold, 18)))

    val chartPanel = new ChartPanel(chart)
    chartPanel.setBackground(Color.white)

    chartPanel
  }

  /**
   * Method that draws a line chart with multiple series representing the evolution of a phenomenon over time
   * (e.g. infections trends).
   * @param infectedPerStage      a map containing the number of people affected by the phenomenon
   *                              for each day (map value) for each stage (map key)
   * @return                      a ChartPanel containing the line chart
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

    //renderer.setSeriesPaint(dataset.getSeriesIndex(legend), Color.RED)
    //renderer.setSeriesLinesVisible(dataset.getSeriesIndex(legend), true)

    //renderer.setSeriesStroke(0, new BasicStroke(2.0f))

    plot.setRenderer(renderer)
    plot.setBackgroundPaint(Color.white)

    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(Color.BLACK)

    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(Color.BLACK)

    chart.getLegend().setFrame(BlockBorder.NONE)
    chart.setTitle(new TextTitle(title, Font("Sans Serif", Font.Bold, 18)))

    val chartPanel = new ChartPanel(chart)
    chartPanel.setBackground(Color.white)

    chartPanel
  }

  /**
   * Method that adds to the specific series the start of a lockdown.
   * @param time          when the lockdown starts
   * @param infections    the number of infections at that time
   */
  def drawLockDownStart(time: Calendar, infections: Int): Unit = {
    startLockdownSeries.add(time \ from, infections)
  }

  /**
   * Method that adds to the specific series the end of a lockdown.
   * @param time          when the lockdown ends
   * @param infections    the number of infections at that time
   */
  def drawLockDownEnd(time: Calendar, infections: Int): Unit = {
    endLockdownSeries.add(time \ from, infections)
  }

  /**
   * Save the line chart in png format.
   */
  def saveChartAsPNG(): Unit = {
    val path = Paths.get("." + File.separator + "sim_res")
    if (!Files.exists(path)) Files.createDirectory(path)
    ChartUtils.saveChartAsPNG(new File("." + File.separator + "sim_res" +
      File.separator + s"linechart_${new Date().toString.replaceAll(":","_")}.png"), chart, 450, 400)
  }

}





