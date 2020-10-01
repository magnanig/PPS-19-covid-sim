package pps.covid_sim.view

import pps.covid_sim.controller.Controller

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

}
