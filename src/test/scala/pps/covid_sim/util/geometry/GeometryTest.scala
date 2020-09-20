package pps.covid_sim.util.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryTest {

  val dimension: Dimension = Dimension(10, 20)
  val rectangle: Rectangle = Rectangle((1, 1), (3, 3))

  @Test
  def testBorderNearness(): Unit = {
    assert(Coordinates(0, 0).onBorder(dimension))
    assert(Coordinates(0, 3).onBorder(dimension))
    assert(Coordinates(3, 0).onBorder(dimension))
    assert(Coordinates(10, 5).onBorder(dimension))
    assert(Coordinates(5, 20).onBorder(dimension))
    assert(Coordinates(10, 20).onBorder(dimension))
    assert(!Coordinates(3, 5).onBorder(dimension))
    assert(!Coordinates(5, 25).onBorder(dimension))

    implicit val tolerance: Double = 0.1
    assert(Coordinates(0, 5).nearBorder(dimension))
    assert(Coordinates(0.1, 5).nearBorder(dimension))
    assert(!Coordinates(0.11, 5).nearBorder(dimension))
    assert(!Coordinates(3, 19.8).nearBorder(dimension))
    assert(Coordinates(3, 19.9).nearBorder(dimension))
  }

  @Test
  def testPointInsideRectangle(): Unit = {
    assert(Coordinates(2, 2).inside(rectangle))
    assert(Coordinates(1, 1).inside(rectangle))
    assert(!Coordinates(0, 0).inside(rectangle))
  }

  @Test
  def testDistanceFromRectangle(): Unit = {
    val tolerance = 0.001 // since working with double

    // Point aligned with rectangle diagonals
    assertEquals(Math.sqrt(2), rectangle - (0, 0), tolerance)
    assertEquals(Math.sqrt(2), rectangle - (4, 4), tolerance)
    assertEquals(Math.sqrt(2), rectangle - (0, 4), tolerance)
    assertEquals(Math.sqrt(2), rectangle - (4, 0), tolerance)

    // Point above rectangle
    assertEquals(1, rectangle - (1, 0), tolerance)
    assertEquals(1, rectangle - (2, 0), tolerance)
    assertEquals(1, rectangle - (3, 0), tolerance)

    // Point under rectangle
    assertEquals(1, rectangle - (1, 4), tolerance)
    assertEquals(1, rectangle - (2, 4), tolerance)
    assertEquals(1, rectangle - (3, 4), tolerance)

    // Point on the left of the rectangle
    assertEquals(1, rectangle - (0, 1), tolerance)
    assertEquals(1, rectangle - (0, 2), tolerance)
    assertEquals(1, rectangle - (0, 3), tolerance)

    // Point on the right of the rectangle
    assertEquals(1, rectangle - (4, 1), tolerance)
    assertEquals(1, rectangle - (4, 2), tolerance)
    assertEquals(1, rectangle - (4, 3), tolerance)
  }

}
