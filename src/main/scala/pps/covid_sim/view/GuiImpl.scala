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
import pps.covid_sim.model.samples.Provinces
import pps.covid_sim.model.simulation.SimulationsManager.classOrdering
import pps.covid_sim.model.simulation.{Simulation, SimulationsManager}
import pps.covid_sim.util.time.Time.ScalaCalendar
import pps.covid_sim.view.viewUtil.Checkers._

import scala.swing.Swing.{CompoundBorder, EmptyBorder, EtchedBorder, TitledBorder}
import scala.swing.TabbedPane.Page
import scala.swing.event.{ButtonClicked, EditDone, SelectionChanged}
import scala.swing.{Action, BorderPanel, BoxPanel, Button, CheckBox, ComboBox, Component, Dialog, FlowPanel, Frame, Label, ListView, MainFrame, Menu, MenuBar, MenuItem, Orientation, SplitPane, TabbedPane, TextField}

class GuiImpl() extends View {

  /**
   * method to set the reference of the controller on the view
   * @param controller that will be setted on the view
   */
  def setController(controller: Controller): Unit = {
    this.controller = controller;
  }
  var controller: Controller = _

  override val tabs: TabbedPane = new TabbedPane {
    import TabbedPane._
    //aggiunta dei diversi grafici
    pages += new Page("Waiting" ,
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("In attesa insrimento parametri..")
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

  var chartSet: Set[LineChart] = _
  var virusStagesChart: LineChart = _
  var weeklyStages: Seq[PieChart] = _
  var barChart: BarChart = _

  //confirm Button
  val confirmButton = new Button("Conferma")

  var saveMenu: MenuBar = _

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
    multipleInfectionProbabilityField.text = "10"
    //val asymptomaticAgeField = new TextField(3)

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
    val beachCheckbox = new CheckBox("Spiagge")
    val squareCheckbox = new CheckBox("Piazze")
    val parkCheckbox = new CheckBox("Parchi")
    val resturantCheckbox = new CheckBox("Ristoranti")
    val pubCheckbox = new CheckBox("Pubs")
    val barCheckbox = new CheckBox("Bar")
    val discoCheckbox = new CheckBox("Discoteche")
    val openDiscoCheckbox = new CheckBox("Discoteche all'aperto")
    val schoolCheckbox = new CheckBox("Scuole")
    val universityCheckBox = new CheckBox("Università")
    val companyCheckbox = new CheckBox("Aziende")
    val factoryCheckbox = new CheckBox("Fabbriche")
    val shopCheckbox = new CheckBox("Negozi")
    val fieldCheckbox = new CheckBox("Campi da Calcio")
    val gymCheckbox = new CheckBox("Palestre")

    val placeAndCheckMap : Map[CheckBox,Class[_ <:Place]]= Map(beachCheckbox->classOf[Beach],squareCheckbox -> classOf[Square],
      parkCheckbox -> classOf[Park], resturantCheckbox -> classOf[Restaurant], pubCheckbox -> classOf[Pub], barCheckbox -> classOf[Bar],
      discoCheckbox -> classOf[Disco], openDiscoCheckbox -> classOf[OpenDisco], schoolCheckbox -> classOf[School], universityCheckBox -> classOf[University],
      companyCheckbox -> classOf[Company], factoryCheckbox -> classOf[Factory], shopCheckbox -> classOf[Shop], gymCheckbox -> classOf[Gym])

    var selectedRegion : Option[Region] = Option.empty
    var selectedProvince : Option[Province] = Option.empty

    /*
     * Create a menu bar with a couple of menus and menu items and
     * set the result as this frame's menu bar.
     */
    saveMenu = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Save Simulation") {
          chartSet.foreach(c=>c.saveChartAsPNG())
          virusStagesChart.saveChartAsPNG()
          weeklyStages.foreach(c=>c.saveChartAsPNG())
          barChart.saveChartAsPNG()
        })
      }
    }
    saveMenu.visible = false
    menuBar = saveMenu

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

        var regionComboboxItems: Seq[String] = Seq(Locality.Italy().name)//ci stanno le regioni
        //val italy : Area = new Locality.Italy()

        val regionSet: Set[Region] = CitiesObject.getRegions
        regionComboboxItems ++= regionSet.map(r=>r.name)

        val provinceSet: Set[Province] = regionSet.flatMap(r => CitiesObject.getProvince(r))

        val provincesSetOfCombobox: Map[Region,ComboBox[String]] = regionSet.map(r=>(r, new ComboBox[String]("Seleziona"+:CitiesObject.getProvince(r).map(p=>p.name).toSeq))).toMap    //regionSet.flatMap(r=> (r => CitiesObject.getProvince(r)))

        provincesSetOfCombobox.foreach(el=>{
          listenTo(el._2.selection)
          reactions += {
            case SelectionChanged(el._2) => {
              print(el._2.selection.item)
              if (el._2.selection.item == "Seleziona") {
                selectedProvince = Option.empty
              } else {
                val provinceSelected: Province = provinceSet.filter(p => p.name == el._2.selection.item).head
                selectedProvince = Option(provinceSelected)
              }
            }
          }
          }
        )

        val regionComboBox: ComboBox[String]= new ComboBox[String](regionComboboxItems)
        //val provinceComboBox: ComboBox[String]= new ComboBox[String](provinceComboboxItems)

        contents += new FlowPanel {
          contents += new Label("<html><p>Simulazione di/dell':</p></html>")
          //val probInfectionField = new TextField(3)
          contents += regionComboBox

          provincesSetOfCombobox.foreach(el=> {
            contents += el._2
            el._2.visible = false;
          })


          //runs
          contents += new Label("   Runs:")
          contents += runsField
          listenTo(runsField, regionComboBox.selection/*, provinceComboBox.selection*/)
          reactions += {
            case EditDone(`runsField`) => checkPositive(runsField)
            case SelectionChanged(`regionComboBox`) => {
                println(regionComboBox.selection.item)
                if (regionComboBox.selection.item == "Italia") {
                  //provinceComboBox.visible = false
                  provincesSetOfCombobox.foreach(el=> {
                    el._2.visible = false;
                  })
                  selectedRegion = Option.empty
                  selectedProvince = Option.empty
                } else {
                  val regionSelected: Region = regionSet.filter(r => r.name == regionComboBox.selection.item).head
                  selectedRegion = Option(regionSelected)

                  provincesSetOfCombobox.foreach(el=> {
                    el._2.visible = false;
                  })
                  provincesSetOfCombobox.foreach(el=> {
                    if(el._1.name == regionSelected.name){
                      el._2.visible = true
                    }
                  })
                }
            }
          }
        }

        contents += new FlowPanel {
          contents += new Label("Data Inizio:")
          contents += dayStartField
          contents += new Label("-")
          contents += monthStartField
          contents += new Label("-")
          contents += yearStartField
          contents += new Label(" Data Fine:")
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
          contents += new Label("<html><p>Probabilità contagio al netto di mascherine e distanziamento sociale:</p></html>")//contagionProbability
          //val probInfectionField = new TextField(3)
          contents += probInfectionField
          contents += new Label("%")

          listenTo(probInfectionField)
          reactions += {
            case EditDone(`probInfectionField`) => checkPercent(probInfectionField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>tempo minimo di guarigione dal virus:</p></html>")//minInfectionDetectionTime
          contents += minHealingTimingField
          contents += new Label("Giorni")
          listenTo(minHealingTimingField)
          reactions += {
            case EditDone(`minHealingTimingField`) => checkPositive(minHealingTimingField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>tempo massimo di guarigione dal virus:</p></html>")//maxInfectionDetectionTime
          contents += maxHealingTimingField
          contents += new Label("Giorni")
          listenTo(maxHealingTimingField)
          reactions += {
            case EditDone(`maxHealingTimingField`) => checkPositive(maxHealingTimingField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>tempo minimo di riconoscimento dal virus:</p></html>")//minInfectionDetectionTime
          contents += minInfectionDetectionTimeField
          contents += new Label("Giorni")
          listenTo(minInfectionDetectionTimeField)
          reactions += {
            case EditDone(`minInfectionDetectionTimeField`) => checkPositive(minInfectionDetectionTimeField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>tempo massimo di riconoscimento dal virus:</p></html>")//maxInfectionDetectionTime
          contents += maxInfectionDetectionTimeField
          contents += new Label("Giorni")
          listenTo(maxInfectionDetectionTimeField)
          reactions += {
            case EditDone(`maxInfectionDetectionTimeField`) => checkPositive(maxInfectionDetectionTimeField)
          }
        }
        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di asintomatici che riescono ad accorgiersi di essere infetti:</p></html>")//asymptomaticDetectionCondProbability
          //val cunningAsymptomaticField = new TextField(3)
          contents += cunningAsymptomaticField
          contents += new Label("%")
          listenTo(cunningAsymptomaticField)
          reactions += {
            case EditDone(`cunningAsymptomaticField`) => checkPercent(cunningAsymptomaticField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>distanza ad di sopra della quale è garantito che il virus non può transitare da persona a persona:</p></html>")//safeZone
          //val distField = new TextField(3)
          contents += distField
          contents += new Label("cm")
          listenTo(distField)
          reactions += {
            case EditDone(`distField`) => checkPositive(distField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di persone che violano l'isolamento(lockdown o accertata positività):</p></html>")//notRespectingIsolationMaxProbability
          //val breakingPeopkeField = new TextField(3)
          contents += breakingPeopkeField
          contents += new Label("%")
          listenTo(breakingPeopkeField)
          reactions += {
            case EditDone(`breakingPeopkeField`) => checkPercent(breakingPeopkeField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di persone che indossano la mascherina nei posti dove richiesto:</p></html>")//minMaskProbability    max è sempre 1
          //val peopleWearingMaskField = new TextField(3)
          contents += peopleWearingMaskField
          contents += new Label("%")
          listenTo(peopleWearingMaskField)
          reactions += {
            case EditDone(`peopleWearingMaskField`) => checkPercent(peopleWearingMaskField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di persone che rispettano le distanze di sicurezza:</p></html>")
          //val peopleSecureDistanceField = new TextField(3)
          contents += peopleSecureDistanceField
          contents += new Label("%")
          listenTo(peopleSecureDistanceField)
          reactions += {
            case EditDone(`peopleSecureDistanceField`) => checkPercent(peopleSecureDistanceField)
          }
        }
        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di persone per far partire il lockdown:</p></html>")//lockDownStart
          //val peopleSecureDistanceField = new TextField(3)
          contents += lockdownStartField
          contents += new Label("%")
          listenTo(lockdownStartField)
          reactions += {
            case EditDone(`lockdownStartField`) => checkPercent(lockdownStartField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>percentuale di persone per fermare lockdown ruspetto max infezioni precedente:</p></html>")//percentage respect with last max infections
          //val peopleSecureDistanceField = new TextField(3)
          contents += lockdownEndField
          contents += new Label("%")
          listenTo(lockdownEndField)
          reactions += {
            case EditDone(`lockdownEndField`) => checkPercent(lockdownEndField)
          }
        }

        contents += new BoxPanel(Orientation.Horizontal) {
          border = CompoundBorder(TitledBorder(EtchedBorder, "Posti chiusi nel LockDown"), EmptyBorder(5, 5, 5, 10))
          //contents += new Label("Quali locali e strutture chiudere")
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
          //contents ++= Seq(beachCheckbox,squareCheckbox,parkCheckbox,resturantCheckbox, pubCheckbox, barCheckbox, discoCheckbox,openDiscoCheckbox,schoolCheckbox, universityCheckBox,companyCheckbox,factoryCheckbox , shopCheckbox, fieldCheckbox,gymCheckbox)
        }

        contents +=  new BorderPanel {
          add(confirmButton, BorderPanel.Position.East)
          listenTo(confirmButton)
          reactions += {
            case ButtonClicked(`confirmButton`) => {

              confirmButton.visible = false;
              //check che i valori obbligatori siano inseriti
              //valori: beachCheckbox,squareCheckbox,parkCheckbox,resturantCheckbox, pubCheckbox, barCheckbox, discoCheckbox,openDiscoCheckbox,schoolCheckbox, universityCheckBox,companyCheckbox,factoryCheckbox , shopCheckbox, fieldCheckbox,gymCheckbox
              //li usi per fare partire la simulazione
              println("confirm!!!!")
              //tabs.pages += new Page("Painting", btnPanel.buttons )
              //tabs.pages += new Page("Painting", btnPanel.buttons )
              var dateFrom: Calendar = ScalaCalendar(yearStartField.text.toInt,monthStartField.text.toInt,dayStartField.text.toInt,0)
              var dateTo: Calendar = ScalaCalendar(yearEndField.text.toInt,monthEndField.text.toInt,dayEndField.text.toInt,0)

              if(dateFrom < dateTo){//controllo che i dates siano corretti
                //println(placeAndCheckMap)
                //println(placeAndCheckMap.filter(el=> el._1.selected).map(el=>el._2).toSet)
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
                  100.toInt/100 ,
                  breakingPeopkeField.text.toDouble/100,
                  lockdownStartField.text.toDouble/100,
                  lockdownEndField.text.toDouble/100,
                  placeAndCheckMap.filter(el=> el._1.selected).map(el=>el._2).toSet)

                var selectedArea: Area = new Locality.Italy
                if(selectedRegion.isDefined && selectedProvince.isDefined) {
                  selectedArea = selectedProvince.get
                }else if (selectedRegion.isDefined){
                  selectedArea = selectedRegion.get
                }
                println(selectedArea)
                controller.startSimulation(selectedArea, dateFrom,dateTo,runsField.text.toInt) // TODO: specificare l'area (es. città o regione...)
              }else{
                Dialog.showMessage(contents.head, "The inserted dates are incorrect!", title="You pressed me")
                confirmButton.visible = true
              }
            }
          }
        }
      }

      val center: SplitPane = new SplitPane(Orientation.Vertical, leftPanel, tabs) {//qui ci metteremo i grafici nelle tabs
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
            tabs.selection.page = list.selection.items(0)
      }
    }
  }

  override def notifyStart: Unit = {
    this.clearTabs()
    saveMenu.visible = false
    this.insertTab(new Page("Waiting" ,
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("In attesa fine simulazione..")
      }))
  //creare i chart
    chartSet = Set(LineChart("Evolution of infections over time", controller.simulationInterval.from, "Days", "Infections", "Infections trend"),
      LineChart("Evolution of recovered over time", controller.simulationInterval.from, "Days", "Recovered", "Recovered trend"),
      /*LineChart("Evolution of deaths over time", controller.simulationInterval.from, "Days", "Deaths", "Deaths trend")*/)

  }

  override def notifyEnd(simulationsManager: SimulationsManager[Simulation] ): Unit = {
    this.clearTabs()
    saveMenu.visible = true

    //chartSet.foreach(c=>this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.infected).toList))))))

    chartSet.foreach(c=> c match {
      case c if c.yAxisLabel=="Infections" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.infected).toList)))))
      case c if c.yAxisLabel=="Recovered" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.recovered).toList)))))
      //case c if c.yAxisLabel=="Deaths" => this.insertTab(new Page(c.title, convertJavaToScalaComponent(c.drawChart(simulationsManager.average(simulationsManager.map(_.deaths).toList)))))
    })
    //aggiungere le heat e gli altri

    virusStagesChart = LineChart("Evolution of infections over time for each stage", controller.simulationInterval.from, "Days", "Infections", "Infections trend")
    //this.insertTab(new Page(virusStagesChart.title, convertJavaToScalaComponent(virusStagesChart.drawMultiSeriesChart(simulationsManager.?????))))//weeklyCovidStages
    barChart = BarChart("Number of infections per place", "Places", "Infections")
    this.insertTab(new Page(barChart.title, convertJavaToScalaComponent(barChart.drawChart(simulationsManager.average(simulationsManager.map(_.infectionPlaces).toList)))))

    simulationsManager.weeklyCovidStages.foreach(w=> this.insertTab(new Page(w._1.getWeekYear.toString+" week",convertJavaToScalaComponent(PieChart(w._1.getWeekYear.toString+" week").drawChart(w._2)))))

    //heat
    this.insertTab(new Page("HeatMap Infections",convertJavaToScalaComponent(new HeatMap().drawMap(simulationsManager.citiesInfection.last._2))))
  }

  override def startLockdown(time: Calendar, infections: Int): Unit = {
    //lineChart.drawLockDownStart(time, infections)
    chartSet.foreach(c=>c.drawLockDownStart(time, infections))
  }

  override def endLockdown(time: Calendar, infections: Int): Unit = {
    //lineChart.drawLockDownEnd(time, infections)
    chartSet.foreach(c=>c.drawLockDownEnd(time, infections))
  }

  private def convertJavaToScalaComponent(panel: JPanel): Component = {
    new Component {
      override lazy val peer: JPanel = panel
    }
  }
}
