package pps.covid_sim.view

import java.awt.Color
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
                     xAxisLabel: String,
                     yAxisLabel: String,
                     legend: String) {

  private var chart: JFreeChart = _
  private var plot: XYPlot = _
  private val dataset: XYSeriesCollection = new XYSeriesCollection()
  private val mainSeries: XYSeries = new XYSeries(legend)
  private val startLockdownSeries: XYSeries = new XYSeries("Start Lockdown")
  private val endLockdownSeries: XYSeries = new XYSeries("End Lockdown")

  private var from: Calendar = _ //

  dataset.addSeries(startLockdownSeries)
  dataset.addSeries(endLockdownSeries)

  def init(from: Calendar): Unit = { this.from = from }

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

    plot = chart.getXYPlot()

    val xAxis = new NumberAxis()
    xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
    plot.setDomainAxis(xAxis)

    val renderer = new XYLineAndShapeRenderer()

    renderer.setSeriesPaint(dataset.getSeriesIndex("Start Lockdown"), Color.BLACK)
    renderer.setSeriesLinesVisible(dataset.getSeriesIndex("Start Lockdown"), false)

    renderer.setSeriesPaint(dataset.getSeriesIndex("End Lockdown"), Color.GREEN)
    renderer.setSeriesLinesVisible(dataset.getSeriesIndex("End Lockdown"), false)

    renderer.setSeriesPaint(dataset.getSeriesIndex(legend), Color.RED)
    renderer.setSeriesLinesVisible(dataset.getSeriesIndex(legend), true)

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
    //chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))
    chartPanel.setBackground(Color.white)

    chartPanel
  }

  def drawLockDownStart(time: Calendar, infections: Int): Unit = {
    startLockdownSeries.add(time \ from, infections)
  }

  def drawLockDownEnd(time: Calendar, infections: Int): Unit = {
    endLockdownSeries.add(time \ from, infections)
  }

  def saveChartAsPNG(): Unit = {
    val path = Paths.get("./sim_res")
    if (!Files.exists(path)) Files.createDirectory(path)
    ChartUtils.saveChartAsPNG(new File(s"./sim_res/linechart_${new Date().toString}.png"), chart, 450, 400)
  }

}




