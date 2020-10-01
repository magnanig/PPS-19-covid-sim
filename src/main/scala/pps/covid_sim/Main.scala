package pps.covid_sim

import pps.covid_sim.controller.ControllerImpl
import pps.covid_sim.view.GuiImp

object Main {
  def main(args: Array[String]): Unit = {
    val c = new ControllerImpl
    val g = new GuiImp(c)
    g.top.pack()
    g.top.centerOnScreen()
    g.top.open()
  }
}
