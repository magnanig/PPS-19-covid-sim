package pps.covid_sim.view

import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.{BorderLayout, Color, Component, Graphics, Graphics2D, RenderingHints, Shape}
import java.awt.geom.{Ellipse2D, Rectangle2D}
import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{City, Province, Region}

import scala.swing.Dimension

/*
object HeatMap {
  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = { // TODO code application logic here
    val forli: City = City(1, "Forlì", 118000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.22268559, 12.04068608)
    val cesena: City = City(1, "Cesena", 98000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.13654899, 12.24217492)
    val fake: City = City(1, "Cesena", 98000, Province(1, "Forlì-Cesena", "FC", Locality.Region.EMILIA_ROMAGNA), 44.80436680, 11.34172080)
    val rimini: City = City(1, "Rimini", 300000, Province(3, "Rimini", "RN", Locality.Region.EMILIA_ROMAGNA), 44.06090086, 12.56562951)
    val bologna: City = City(1, "Bologna", 1118000, Province(4, "Bologna", "BO", Locality.Region.EMILIA_ROMAGNA), 44.49436680, 11.34172080)

    val infectionsInADay = Map(forli -> 100 , cesena -> 4890, rimini -> 15001, bologna -> 800000, fake -> 100)

    //new HeatMap().drawMap(infectionsInADay)

    val frame = new JFrame() {
      this.setSize(1000, 1000)
      this.setPreferredSize(new Dimension(1000, 1000))
      this.setTitle("Drawing tings")
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      this.add(new HeatMap().drawMap(infectionsInADay), BorderLayout.CENTER)
      this.setVisible(true)
    }

  }
}
*/


class HeatMap() {

  def drawMap(infectionsInADay: Map[City, Int]): JPanel = {
    new GraphicsPanel(infectionsInADay)
  }

  class GraphicsPanel(infectionsInADay: Map[City, Int]) extends JPanel {
    setLayout(new BorderLayout)
    this.setPreferredSize(new Dimension(1000, 1000))
    this.add(new GraphicsComponent(infectionsInADay), BorderLayout.CENTER)
    revalidate()
    repaint()
    this.setVisible(true)

    private class GraphicsComponent(infectionsInADay: Map[City, Int]) extends JComponent {
      private val italyOutlineMap: BufferedImage = ImageIO.read(new File("./res/italy_outline_map.png"))
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
          println(elem._1.name, x, y)
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

      private def computeSpotColor(infectedRatio: Double): Color = infectedRatio match {
        case infectedRatio if (infectedRatio < 2.0) => Color.green
        case infectedRatio if (infectedRatio < 5.0) => Color.yellow
        case infectedRatio if (infectedRatio < 10.0) => Color.orange
        case _ => Color.red
      }

      private def computeSpotDimension(numResidents: Int): Int = numResidents match {
        case numResidents if (numResidents < 100000) => 5
        case numResidents if (numResidents < 300000) => 10
        case numResidents if (numResidents < 500000) => 20
        case _ => 30
      }

      def convertGpsCoordsToMapCoords(longitude: Double, latitude: Double): (Double, Double) = {
        val x = (longitude - mapLongitudeLeft) * (mapWidth / mapLongitudeDelta)

        val worldMapWidth = ((mapWidth / mapLongitudeDelta) * 360) / (2 * Math.PI)
        val mapOffsetY = worldMapWidth / 2 * Math.log((1 + Math.sin(mapLatitudeBottomDegree)) /
          (1 - Math.sin(mapLatitudeBottomDegree)))
        val y = mapHeight - ((worldMapWidth / 2 * Math.log((1 + Math.sin(latitude * Math.PI / 180))
          / (1 - Math.sin(latitude * Math.PI / 180)))) - mapOffsetY)

        (x, y)
      }

    }
  }

}