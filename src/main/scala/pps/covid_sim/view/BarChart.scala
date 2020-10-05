package pps.covid_sim.view

import org.jfree.chart.{ChartFactory, ChartPanel, ChartUtils, JFreeChart}
import pps.covid_sim.model.places.Place
import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Date

import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import pps.covid_sim.model.places.Locations.Location

import scala.collection.SortedMap

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

  /**
   * Method that draws a bar chart representing the distribution of the infected in the different places.
   * @param infectionPlaces     a map containing the number of infected (map value) for each place (map key)
   * @return                    a ChartPanel containing the bar chart
   */
  def drawChart(infectionPlaces: SortedMap[Class[_ <: Location], Int]): ChartPanel = {
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

  /**
   * Save the bar chart in png format.
   */
  def saveChartAsPNG(): Unit = {
    val path = Paths.get("." + File.separator + "sim_res")
    if (!Files.exists(path)) Files.createDirectory(path)
    ChartUtils.saveChartAsPNG(new File("." + File.separator + "sim_res" +
      File.separator + s"barchart_${new Date().toString.replaceAll(":","_")}.png"), chart, 450, 400)
  }

}


