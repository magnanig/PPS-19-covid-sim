package pps.covid_sim.view

import org.jfree.chart.{ChartFactory, ChartPanel, ChartUtils, JFreeChart}
import pps.covid_sim.model.places.Place
import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Date

import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset

/**
 * Class that manages the creation of a bar chart.
 * @param title         the title of the chart
 * @param xAxisLabel    the label of the x-axis
 * @param yAxisLabel    the label of the y-axis
 */
case class BarChart(title: String,
                    xAxisLabel: String,
                    yAxisLabel: String) {

  private var chart: JFreeChart = _
  private val dataset: DefaultCategoryDataset = new DefaultCategoryDataset()

  def drawChart(infectionPlaces: Map[Class[_ <: Place], Int]): ChartPanel = {
    infectionPlaces.foreach(elem => dataset.addValue(elem._2, elem._1.getSimpleName, elem._1.getSimpleName))

    chart = ChartFactory.createBarChart(
      title,
      xAxisLabel,
      yAxisLabel,
      dataset,
      PlotOrientation.VERTICAL,
      true, true, false)

    val chartPanel = new ChartPanel(chart)
    chartPanel
  }

  def saveChartAsPNG(): Unit = {
    val path = Paths.get("./sim_res")
    if (!Files.exists(path)) Files.createDirectory(path)
    ChartUtils.saveChartAsPNG(new File(s"./sim_res/barchart_${new Date().toString}.png"), chart, 450, 400)
  }

}


