package pps.covid_sim

import pps.covid_sim.view.GuiImp

object Main {
  def main(args: Array[String]): Unit = {
    val g = new GuiImp
    g.top.pack()
    g.top.centerOnScreen()
    g.top.open()
  }
}
