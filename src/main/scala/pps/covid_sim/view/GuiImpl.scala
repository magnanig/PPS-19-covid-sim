package pps.covid_sim.view
import java.util.Calendar

import javax.swing.JPanel
import pps.covid_sim.controller.Controller
import pps.covid_sim.model.creation.CitiesObject
import pps.covid_sim.model.places.Education.{School, University}
import pps.covid_sim.model.places.FreeTime._
import pps.covid_sim.model.places.Hobbies.Gym
import pps.covid_sim.model.places.Jobs.{Company, Factory}
import pps.covid_sim.model.places.Locality.{Area, Province, Region}
import pps.covid_sim.model.places.OpenPlaces.{Beach, Park, Square}
import pps.covid_sim.model.places.Shops.Shop
import pps.covid_sim.model.places.{Locality, Place}
import pps.covid_sim.model.simulation.SimulationsManager.classOrdering
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.view.charts.{BarChart, HeatMap, LineChart, PieChart}
import pps.covid_sim.view.viewUtil.Checkers._

import scala.swing.Swing.{CompoundBorder, EmptyBorder, EtchedBorder, TitledBorder}
import scala.swing.TabbedPane.Page
import scala.swing.event.{ButtonClicked, EditDone, SelectionChanged}
import scala.swing.{BorderPanel, BoxPanel, Button, CheckBox, ComboBox, Component, Dialog, FlowPanel, Frame, Label, ListView, MainFrame, Orientation, SplitPane, TabbedPane, TextField}

class GuiImpl() extends View {

  /**
   * method to set the reference of the controller on the view
   * @param controller that will be setted on the view
   */
  def setController(controller: Controller): Unit = {
    this.controller = controller
  }
  var controller: Controller = _

  override val tabs: TabbedPane = new TabbedPane {
    import TabbedPane._
    //aggiunta dei diversi grafici
    pages += new Page("Waiting" ,
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("Waiting for input parameters...")
      })
  }

  override def insertTab(page: TabbedPane.Page): Unit = {
    if(tabs.pages.forall(p=>p.title!= page.title)){
      tabs.pages += page
    }
  }

  override def substituteTab(page: TabbedPane.Page): Unit = {
    if(tabs.pages.exists(p=>p.title== page.title)){
      this.removeTab(page.title)
      tabs.pages += page
    }
  }

  override def removeTab(title: String): Unit = {
    tabs.pages.filter(p=> p.title != title)
  }

  override def clearTabs(): Unit = {
    tabs.pages.clear()
  }

  override def setVisibleConfirmButton(): Unit = {
    confirmButton.visible = true
  }

  var chartSet: Set[LineChart] = Set()
  var virusStagesChart: LineChart = _
  var weeklyStages: Seq[PieChart] = Seq()
  var barChart: BarChart = _

  //confirm Button
  val confirmButton = new Button("Confirm")

  override val top: Frame = new MainFrame {
    title = "PPS-19-Covid-Sim"

    //TextBoxes
    val probInfectionField = new TextField(3)
    probInfectionField.text = "70"
    val minHealingTimingField = new TextField(4)
    minHealingTimingField.text = "7"
    val maxHealingTimingField = new TextField(4)
    maxHealingTimingField.text = "40"
    val minInfectionDetectionTimeField = new TextField(4)
    minInfectionDetectionTimeField.text = "2"
    val maxInfectionDetectionTimeField = new TextField(4)
    maxInfectionDetectionTimeField.text = "7"
    val multipleInfectionProbabilityField = new TextField(3)
    multipleInfectionProbabilityField.text = "0"

    val cunningAsymptomaticField = new TextField(3)
    cunningAsymptomaticField.text = "20"
    val distField = new TextField(3)
    distField.text = "150"
    val breakingPeopkeField = new TextField(3)
    breakingPeopkeField.text = "40"
    val peopleWearingMaskField = new TextField(3)
    peopleWearingMaskField.text = "30"
    val peopleSecureDistanceField = new TextField(3)
    peopleSecureDistanceField.text = "80"
    val lockdownStartField = new TextField(3)
    lockdownStartField.text = "10"
    val lockdownEndField = new TextField(2)
    lockdownEndField.text = "80"

    //dates
    val dayStartField = new TextField(2)
    dayStartField.text = "1"
    val monthStartField = new TextField(2)
    monthStartField.text = "1"
    val yearStartField = new TextField(4)
    yearStartField.text = "2020"
    val dayEndField = new TextField(2)
    dayEndField.text = "1"
    val monthEndField = new TextField(2)
    monthEndField.text = "12"
    val yearEndField = new TextField(4)
    yearEndField.text = "2020"

    //runs
    val runsField = new TextField(3)
    runsField.text = "1"

    //CheckBoxes
    val beachCheckbox = new CheckBox("Beaches")
    val squareCheckbox = new CheckBox("Squares")
    val parkCheckbox = new CheckBox("Parks")
    val resturantCheckbox = new CheckBox("Restaurants")
    val pubCheckbox = new CheckBox("Pubs")
    val barCheckbox = new CheckBox("Bars")
    val discoCheckbox = new CheckBox("Discos")
    val openDiscoCheckbox = new CheckBox("Open discos")
    val schoolCheckbox = new CheckBox("Schools")
    val universityCheckBox = new CheckBox("Universities")
    val companyCheckbox = new CheckBox("Companies")
    val factoryCheckbox = new CheckBox("Factories")
    val shopCheckbox = new CheckBox("Shops")
    val fieldCheckbox = new CheckBox("Soccer fields")
    val gymCheckbox = new CheckBox("Gyms")

    val placeAndCheckMap : Map[CheckBox,Class[_ <:Place]]= Map(beachCheckbox->classOf[Beach],squareCheckbox -> classOf[Square],
      parkCheckbox -> classOf[Park], resturantCheckbox -> classOf[Restaurant], pubCheckbox -> classOf[Pub], barCheckbox -> classOf[Bar],
      discoCheckbox -> classOf[Disco], openDiscoCheckbox -> classOf[OpenDisco], schoolCheckbox -> classOf[School], universityCheckBox -> classOf[University],
      companyCheckbox -> classOf[Company], factoryCheckbox -> classOf[Factory], shopCheckbox -> classOf[Shop], gymCheckbox -> classOf[Gym])

    var selectedRegion : Option[Region] = Option.empty
    var selectedProvince : Option[Province] = Option.empty

    contents = new BorderPanel {
      import BorderPanel.Position._

      val list: ListView[TabbedPane.Page] = new ListView(tabs.pages) {
        selectIndices(0)
        selection.intervalMode = ListView.IntervalMode.Single
        renderer = ListView.Renderer(_.title)
      }

      /**
       * this panel contains the form that allow the user to insert specific parameters for the simulation
       * - probabilità contagio al netto di mascherine e distanziamento sociale
       * - tempo minimo e massimo di guarigione dal virus
       * - percentuale stimata di asintomatici per facie di età
       * - percentuale di asintomatici che riescono ad accorgiersi di essere infetti
       * - distanza ad di sopra della quale è garantito che il virus non può transitare da persona a persona
       * - percentuale di persone che violano l'isolamento( sia che esso sia dovuto ad un lockdown, cia per via della lora accertata positività)
       * - percentuale di persone che indossano la mascherina nei posti dove richiesto
       * - percentuale di persone che rispettano le distanze di sicurezza
       * - scelta di quali locali e strutture chiudere [CheckBoxes]
       */
      val leftPanel: BoxPanel = new BoxPanel(Orientation.Vertical){

        var regionComboboxItems: Seq[String] = Seq(Locality.Italy().name)
        val regionSet: Set[Region] = CitiesObject.getRegions
        regionComboboxItems ++= regionSet.map(r=>r.name)

        val provinceSet: Set[Province] = regionSet.flatMap(r => CitiesObject.getProvince(r))
        val provincesSetOfCombobox: Map[Region,ComboBox[String]] = regionSet.map(r=>(r, new ComboBox[String]("Seleziona"+:CitiesObject.getProvince(r).map(p=>p.name).toSeq))).toMap    //regionSet.flatMap(r=> (r => CitiesObject.getProvince(r)))

        provincesSetOfCombobox.foreach(el=>{
          listenTo(el._2.selection)
          reactions += {
            case SelectionChanged(el._2) =>
              if (el._2.selection.item == "Seleziona") {
                selectedProvince = Option.empty
              } else {
                val provinceSelected: Province = provinceSet.filter(p => p.name == el._2.selection.item).head
                selectedProvince = Option(provinceSelected)
              }
          }
        })

        val regionComboBox: ComboBox[String]= new ComboBox[String](regionComboboxItems)

        contents += new FlowPanel {
          contents += new Label("<html><p>Simulation of:</p></html>")
          contents += regionComboBox

          provincesSetOfCombobox.foreach(el=> {
            contents += el._2
            el._2.visible = false
          })

          //runs
          contents += new Label("   Runs:")
          contents += runsField
          listenTo(runsField, regionComboBox.selection/*, provinceComboBox.selection*/)
          reactions += {
            case EditDone(`runsField`) => checkPositive(runsField)
            case SelectionChanged(`regionComboBox`) =>
              if (regionComboBox.selection.item == "Italia") {
                provincesSetOfCombobox.foreach(el=> {
                  el._2.visible = false
                })
                selectedRegion = Option.empty
                selectedProvince = Option.empty
              } else {
                val regionSelected: Region = regionSet.filter(r => r.name == regionComboBox.selection.item).head
                selectedRegion = Option(regionSelected)

                provincesSetOfCombobox.foreach(el=> {
                  el._2.visible = false
                })
                provincesSetOfCombobox.foreach(el=> {
                  if(el._1.name == regionSelected.name){
                    el._2.visible = true
                  }
                })
              }
          }
        }

        contents += new FlowPanel {
          contents += new Label("Start date:")
          contents += dayStartField
          contents += new Label("-")
          contents += monthStartField
          contents += new Label("-")
          contents += yearStartField
          contents += new Label(" End date:")
          contents += dayEndField
          contents += new Label("-")
          contents += monthEndField
          contents += new Label("-")
          contents += yearEndField

          listenTo(dayStartField,monthStartField,yearStartField,dayEndField,monthEndField,yearEndField)
          reactions += {
            case EditDone(`dayStartField`) => checkDay(dayStartField)
            case EditDone(`monthStartField`) => checkMonth(monthStartField)
            case EditDone(`yearStartField`) => checkPositive(yearStartField)
            case EditDone(`dayEndField`) => checkDay(dayEndField)
            case EditDone(`monthEndField`) => checkMonth(monthEndField)
            case EditDone(`yearEndField`) => checkPositive(yearEndField)
          }
        }
        contents += new FlowPanel {
          contents += new Label("<html><p>Probability of contagion net of masks and social distancing:</p></html>")//contagionProbability
          contents += probInfectionField
          contents += new Label("%")

          listenTo(probInfectionField)
          reactions += {
            case EditDone(`probInfectionField`) => checkPercent(probInfectionField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Probability of multiple infections:</p></html>")//contagionProbability
          contents += multipleInfectionProbabilityField
          contents += new Label("%")

          listenTo(multipleInfectionProbabilityField)
          reactions += {
            case EditDone(`multipleInfectionProbabilityField`) => checkPercent(multipleInfectionProbabilityField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Minimum time of recovery from the virus:</p></html>")//minInfectionDetectionTime
          contents += minHealingTimingField
          contents += new Label("days")
          listenTo(minHealingTimingField)
          reactions += {
            case EditDone(`minHealingTimingField`) => checkPositive(minHealingTimingField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Maximum time of recovery from the virus:</p></html>")//maxInfectionDetectionTime
          contents += maxHealingTimingField
          contents += new Label("days")
          listenTo(maxHealingTimingField)
          reactions += {
            case EditDone(`maxHealingTimingField`) => checkPositive(maxHealingTimingField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Minimum time to detect the virus:</p></html>")//minInfectionDetectionTime
          contents += minInfectionDetectionTimeField
          contents += new Label("days")
          listenTo(minInfectionDetectionTimeField)
          reactions += {
            case EditDone(`minInfectionDetectionTimeField`) => checkPositive(minInfectionDetectionTimeField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Maximum time to detect the virus:</p></html>")//maxInfectionDetectionTime
          contents += maxInfectionDetectionTimeField
          contents += new Label("days")
          listenTo(maxInfectionDetectionTimeField)
          reactions += {
            case EditDone(`maxInfectionDetectionTimeField`) => checkPositive(maxInfectionDetectionTimeField)
          }
        }
        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of asymptomatic of people who realize they are infected:</p></html>")//asymptomaticDetectionCondProbability
          contents += cunningAsymptomaticField
          contents += new Label("%")
          listenTo(cunningAsymptomaticField)
          reactions += {
            case EditDone(`cunningAsymptomaticField`) => checkPercent(cunningAsymptomaticField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Distance above which it is guaranteed that the virus cannot be transmitted between people:</p></html>")//safeZone
          contents += distField
          contents += new Label("cm")
          listenTo(distField)
          reactions += {
            case EditDone(`distField`) => checkPositive(distField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of people that violate isolation (lockdown or positivity ascertained):</p></html>")//notRespectingIsolationMaxProbability
          contents += breakingPeopkeField
          contents += new Label("%")
          listenTo(breakingPeopkeField)
          reactions += {
            case EditDone(`breakingPeopkeField`) => checkPercent(breakingPeopkeField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of people who wears mask where required:</p></html>")//minMaskProbability
          contents += peopleWearingMaskField
          contents += new Label("%")
          listenTo(peopleWearingMaskField)
          reactions += {
            case EditDone(`peopleWearingMaskField`) => checkPercent(peopleWearingMaskField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of people who respect safety distances:</p></html>")
          contents += peopleSecureDistanceField
          contents += new Label("%")
          listenTo(peopleSecureDistanceField)
          reactions += {
            case EditDone(`peopleSecureDistanceField`) => checkPercent(peopleSecureDistanceField)
          }
        }
        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of infected people needed to start lockdown:</p></html>")//lockDownStart
          contents += lockdownStartField
          contents += new Label("%")
          listenTo(lockdownStartField)
          reactions += {
            case EditDone(`lockdownStartField`) => checkPercent(lockdownStartField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>Percentage of infected people needed to end lockdown (compared to last peak of contagious):</p></html>")//percentage respect with last max infections
          contents += lockdownEndField
          contents += new Label("%")
          listenTo(lockdownEndField)
          reactions += {
            case EditDone(`lockdownEndField`) => checkPercent(lockdownEndField)
          }
        }

        contents += new BoxPanel(Orientation.Horizontal) {
          border = CompoundBorder(TitledBorder(EtchedBorder, "Closed places during lockdown"), EmptyBorder(5, 5, 5, 10))
          contents += new Component {

            contents += new BoxPanel(Orientation.Vertical) {
              contents ++= Seq(beachCheckbox,squareCheckbox,parkCheckbox,resturantCheckbox, pubCheckbox)
            }
            contents += new BoxPanel(Orientation.Vertical) {
              contents ++= Seq( barCheckbox, discoCheckbox,openDiscoCheckbox,schoolCheckbox, universityCheckBox)
            }
            contents += new BoxPanel(Orientation.Vertical) {
              contents ++= Seq(companyCheckbox,factoryCheckbox , shopCheckbox, fieldCheckbox,gymCheckbox)
            }
          }
        }

        contents +=  new BorderPanel {
          add(confirmButton, BorderPanel.Position.East)
          listenTo(confirmButton)
          reactions += {
            case ButtonClicked(`confirmButton`) =>

              confirmButton.visible = false
              //check che i valori obbligatori siano inseriti
              val dateFrom: Calendar = ScalaCalendar(yearStartField.text.toInt,monthStartField.text.toInt,dayStartField.text.toInt)
              val dateTo: Calendar = ScalaCalendar(yearEndField.text.toInt,monthEndField.text.toInt,dayEndField.text.toInt)

              if(dateFrom < dateTo){//controllo che i dates siano corretti
                controller.setSimulationParameters(
                  distField.text.toDouble/10,
                  minHealingTimingField.text.toInt,
                  maxHealingTimingField.text.toInt,
                  minInfectionDetectionTimeField.text.toInt,
                  maxInfectionDetectionTimeField.text.toInt,
                  multipleInfectionProbabilityField.text.toDouble/100,
                  50.toDouble/100,
                  cunningAsymptomaticField.text.toDouble/100,
                  probInfectionField.text.toDouble/100,
                  peopleWearingMaskField.text.toDouble/100 ,
                  1,//100.toInt/100
                  breakingPeopkeField.text.toDouble/100,
                  lockdownStartField.text.toDouble/100,
                  lockdownEndField.text.toDouble/100,
                  placeAndCheckMap.filter(el => el._1.selected).values.toSet)

                var selectedArea: Area = new Locality.Italy
                if(selectedRegion.isDefined && selectedProvince.isDefined) {
                  selectedArea = selectedProvince.get
                }else if (selectedRegion.isDefined){
                  selectedArea = selectedRegion.get
                }
                controller.startSimulation(selectedArea, dateFrom,dateTo,runsField.text.toInt)
              }else{
                Dialog.showMessage(contents.head, "The inserted dates are incorrect!", title="Warning")
                confirmButton.visible = true
              }
          }
        }
      }

      val center: SplitPane = new SplitPane(Orientation.Vertical, leftPanel, tabs) {
        oneTouchExpandable = true
        continuousLayout = true
      }
      layout(center) = Center
      listenTo(tabs.selection)
      listenTo(list.selection)
      reactions += {
        case SelectionChanged(`tabs`) =>
          list.selectIndices(tabs.selection.index)
        case SelectionChanged(`list`) =>
          if (list.selection.items.length == 1)
            tabs.selection.page = list.selection.items.head
      }
    }
  }

  override def notifyEndRun(simulation: Simulation ): Unit = {
    chartSet.foreach {
      case c if c.yAxisLabel == "Infections" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulation.infected))))
      case c if c.yAxisLabel == "Recovered" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulation.recovered))))
    }
  }

  override def notifyStart(): Unit = {
    this.clearTabs()
    this.insertTab(new Page("Waiting" ,
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("Waiting for simulation...")
      }))
  //creare i chart
    chartSet = Set(LineChart("Evolution of infections over time", controller.simulationInterval.from, "Days", "Infections", "Infections trend"),
      LineChart("Evolution of recovered over time", controller.simulationInterval.from, "Days", "Recovered", "Recovered trend"))

  }

  override def notifyEnd(simulationsManager: SimulationsManager[Simulation] ): Unit = {
    this.clearTabs()

    chartSet.foreach {
      case c if c.yAxisLabel == "Infections" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.infected).toList),avg = true))))
      case c if c.yAxisLabel == "Recovered" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.recovered).toList),avg = true))))
    }

    virusStagesChart = LineChart("Evolution of infections over time for each stage", controller.simulationInterval.from, "Days", "Infections", "Infections trend")
    this.insertTab(new Page(virusStagesChart.title, convertJavaToScalaComponent(virusStagesChart.drawMultiSeriesChart(simulationsManager.dayCovidStages))))
    barChart = BarChart("Number of infections per place", "Places", "Infections")
    this.insertTab(new Page(barChart.title, convertJavaToScalaComponent(barChart.drawChart(simulationsManager.average(simulationsManager.map(_.infectionPlaces).toList)))))

    //Pie
    simulationsManager.weeklyCovidStages.zipWithIndex.foreach(w=>{
      val pie = PieChart("Week "+ (w._2+1))
      this.insertTab(new Page("Week "+ (w._2+1),convertJavaToScalaComponent(pie.drawChart(w._1._2))))
      weeklyStages :+ pie
    })

    //Heat
    this.insertTab(new Page("HeatMap Infections",convertJavaToScalaComponent(new HeatMap().drawMap(simulationsManager.citiesInfection.last._2))))
  }

  override def startLockdown(time: Calendar, infections: Int): Unit = {
    chartSet.find(_.yAxisLabel == "Infections").get.drawLockDownStart(time, infections)
  }

  override def endLockdown(time: Calendar, infections: Int): Unit = {
    chartSet.find(_.yAxisLabel == "Infections").get.drawLockDownEnd(time, infections)
  }

  private def convertJavaToScalaComponent(panel: JPanel): Component = {
    new Component {
      override lazy val peer: JPanel = panel
    }
  }
}
