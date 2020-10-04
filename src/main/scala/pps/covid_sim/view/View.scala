package pps.covid_sim.view

import java.util.Calendar

import pps.covid_sim.controller.Controller
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}

import scala.swing.{Frame, SimpleSwingApplication, TabbedPane}
import scala.swing.TabbedPane.Page

trait View extends SimpleSwingApplication{
  val tabs: TabbedPane

  /**
   * Insert a new page if it's Title is not already present
   * @param page that will be added
   */
  def insertTab(page: Page): Unit

  /**
   * Sobstitute a new page having the same Title of the one passed as param (if present)
   * @param page
   */
  def substituteTab(page: Page): Unit

  /**
   * Remove a Page having the specified Title if present
   * @param title of the page that will be deleted
   */
  def removeTab(title: String): Unit

  /**
   * Clear all Pages
   */
  def clearTabs(): Unit

  /**
   * Method that set visible the button if it was not
   */
  def setVisibleConfirmButton(): Unit

  /**
   * Notify the gui that the simulation started.
   */
  def notifyStart: Unit

  /**
   * Notify the gui that the simulation ended.
   */
  def notifyEnd(simulationsManager: SimulationsManager[Simulation]): Unit


  def startLockdown(time: Calendar, infections: Int): Unit

  def endLockdown(time: Calendar, infections: Int): Unit

}
