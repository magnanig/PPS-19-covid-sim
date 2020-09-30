package pps.covid_sim.view

import scala.swing.{Frame, SimpleSwingApplication, TabbedPane}
import scala.swing.TabbedPane.Page

trait View extends SimpleSwingApplication{
  val tabs: TabbedPane

  def insertTab(page: Page)
  def substituteTabe(page: Page)
  def remuveTab(title: String)
  def clearTabs()

}
