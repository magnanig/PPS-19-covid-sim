package pps.covid_sim.view.charts

import java.awt.Color

import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.data.category.DefaultCategoryDataset
import pps.covid_sim.model.places.Locations.Location

import scala.collection.SortedMap

/**
 * Class that manages the creation of a bar chart.
 *
 * @param title      the title of the chart
 * @param xAxisLabel the label of the x-axis
 * @param yAxisLabel the label of the y-axis
 */
case class BarChart(title: String,
                    xAxisLabel: String,
                    yAxisLabel: String) {

  private var chart: JFreeChart = _
  private val dataset: DefaultCategoryDataset = new DefaultCategoryDataset()

  /**
   * Method that draws a bar chart representing the distribution of the infected in the different places.
   *
   * @param infectionPlaces a map containing the number of infected (map value) for each place (map key)
   * @return a ChartPanel containing the bar chart
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

    val plot = chart.getCategoryPlot
    plot.setBackgroundPaint(Color.white)
    plot.setDomainGridlinePaint(Color.black)
    plot.setRangeGridlinePaint(Color.black)

    new ChartPanel(chart)
  }

}
