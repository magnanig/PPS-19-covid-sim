package pps.covid_sim.view

import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.{BorderLayout, Color, Graphics, Graphics2D, RenderingHints}
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.parameters.CreationParameters

import scala.collection.SortedMap
import scala.swing.Dimension

/**
 * Class that manages the creation of a heat map.
 */
class HeatMap() {

  private val italyOutlineMap: BufferedImage = ImageIO.read(new File("./res/italy_outline_map.png"))

  /**
   * Method that draws a heat map representing the situation of the epidemic spread at a certain time.
   * @param infectionsInADay      a map containing the number of infection (map value) of a given city (map key)
   *                              on a certain day
   * @return                      a JPanel containing the heat map
   */
  def drawMap(infectionsInADay: SortedMap[City, Int]): JPanel = {
    new GraphicsPanel(infectionsInADay)
  }

  class GraphicsPanel(infectionsInADay: SortedMap[City, Int]) extends JPanel {
    setLayout(new BorderLayout)
    this.setPreferredSize(new Dimension(1000, 1000))
    this.add(new GraphicsComponent(infectionsInADay), BorderLayout.CENTER)
    revalidate()
    repaint()
    this.setVisible(true)

    private class GraphicsComponent(infectionsInADay: SortedMap[City, Int]) extends JComponent {

      private val mapWidth: Int = italyOutlineMap.getWidth
      private val mapHeight: Int = italyOutlineMap.getHeight

      private val mapLongitudeLeft: Double = 6.60050504 //6.62572963
      private val mapLongitudeRight: Double = 18.93483848 //18.52069585
      private val mapLongitudeDelta: Double = mapLongitudeRight - mapLongitudeLeft

      private val mapLatitudeBottom: Double = 35.48805894 //36.64648834
      private val mapLatitudeBottomDegree: Double = mapLatitudeBottom * Math.PI / 180

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        val g2 = g.asInstanceOf[Graphics2D]

        infectionsInADay.foreach(elem => {
          val (x, y) = convertGpsCoordsToMapCoords(elem._1.longitude, elem._1.latitude)

          g2.drawImage(italyOutlineMap, 0, 0, null)
          val spotColor: Color = computeSpotColor((elem._2 * 100) / elem._1.numResidents)
          g2.setColor(spotColor)
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
          val spotDimension = computeSpotDimension(elem._1.numResidents)
          val shape: Ellipse2D.Double = new Ellipse2D.Double(x - (spotDimension / 2), y - (spotDimension / 2),
            spotDimension, spotDimension)
          g2.fill(shape)
        })
        g2.dispose()
      }

      /**
       * Method that converts the GPS coordinates of a city in the coordinates in pixels
       * of the Italian map used for the heat map. Thanks to https://stackoverflow.com/a/10401734
       * @param longitude     the city longitude
       * @param latitude      the city latitude
       * @return              the coordinates in pixels
       */
      def convertGpsCoordsToMapCoords(longitude: Double, latitude: Double): (Double, Double) = {
        val x = (longitude - mapLongitudeLeft) * (mapWidth / mapLongitudeDelta)

        val worldMapWidth = ((mapWidth / mapLongitudeDelta) * 360) / (2 * Math.PI)
        val mapOffsetY = worldMapWidth / 2 * Math.log((1 + Math.sin(mapLatitudeBottomDegree)) /
          (1 - Math.sin(mapLatitudeBottomDegree)))
        val y = mapHeight - ((worldMapWidth / 2 * Math.log((1 + Math.sin(latitude * Math.PI / 180))
          / (1 - Math.sin(latitude * Math.PI / 180)))) - mapOffsetY)

        (x, y)
      }

      private def computeSpotColor(infectedRatio: Double): Color = infectedRatio match {
        case infectedRatio if infectedRatio < 2.0 => Color.green
        case infectedRatio if infectedRatio < 5.0 => Color.yellow
        case infectedRatio if infectedRatio < 10.0 => Color.orange
        case _ => Color.red
      }

      private def computeSpotDimension(numResidents: Int): Int = numResidents match {
        case numResidents if numResidents < 100000 * CreationParameters.citizensPercentage => 5
        case numResidents if numResidents < 300000 * CreationParameters.citizensPercentage => 10
        case numResidents if numResidents < 500000 * CreationParameters.citizensPercentage => 20
        case _ => 30
      }

    }

  }

}



