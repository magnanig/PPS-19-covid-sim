package pps.covid_sim.view

import java.awt.Color
import java.io.File
import java.nio.file.{Files, Paths}
import java.text.DecimalFormat
import java.util.Date

import org.jfree.chart.labels.StandardPieSectionLabelGenerator
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.{ChartFactory, ChartPanel, ChartUtils, JFreeChart}
import org.jfree.data.general.DefaultPieDataset

/**
 * Class that manages the creation of a pie chart.
 * @param title   the title of the pie chart
 */
case class PieChart(title: String) {

  private var chart: JFreeChart = _
  private val dataset: DefaultPieDataset = new DefaultPieDataset()

  /**
   * Method that draws a pie chart representing the distribution of infected in the different stages.
   * @param infectionStages     a map containing the number of infected (map value) for each stage (map key)
   * @return                    a ChartPanel containing the chart
   */
  def drawChart(infectionStages: Map[Int, Int]): ChartPanel = {
    infectionStages.foreach(elem => dataset.setValue(elem._1, elem._2))

    chart = ChartFactory.createPieChart(
      title,
      dataset,
      true, true, false)

    val labelGenerator = new StandardPieSectionLabelGenerator(
      "Stage {0}: {1} people ({2})", new DecimalFormat("0"), new DecimalFormat("0%"))

    val plot =  chart.getPlot
    plot.setBackgroundPaint(Color.white)

    val piePlot = plot.asInstanceOf[PiePlot]
    piePlot.setLabelGenerator(labelGenerator)

    piePlot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("Stage {0}"))

    val chartPanel = new ChartPanel(chart)
    chartPanel
  }

  /**
   * Save the pie chart in png format.
   */
  def saveChartAsPNG(): Unit = {
    val path = Paths.get("." + File.separator + "sim_res")
    if (!Files.exists(path)) Files.createDirectory(path)
    ChartUtils.saveChartAsPNG(new File("." + File.separator + "sim_res" +
      File.separator + s"piechart_${new Date().toString}.png"), chart, 450, 400)
  }

}

