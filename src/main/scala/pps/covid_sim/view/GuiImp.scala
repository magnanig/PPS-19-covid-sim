package pps.covid_sim.view
import java.util.Calendar

import pps.covid_sim.controller.Controller
import pps.covid_sim.util.time.Time.ScalaCalendar

import scala.swing.Swing.{CompoundBorder, EmptyBorder, EtchedBorder, TitledBorder}
import scala.swing.event.{ButtonClicked, EditDone, SelectionChanged}
import scala.swing.{Action, BorderPanel, BoxPanel, Button, ButtonGroup, CheckBox, CheckMenuItem, ComboBox, FlowPanel, Frame, Label, ListView, MainFrame, Menu, MenuBar, MenuItem, Orientation, RadioButton, RadioMenuItem, Separator, SplitPane, Swing, TabbedPane, TextField}

class GuiImp(controller: Controller) extends View {
  override val tabs: TabbedPane = new TabbedPane {
    import TabbedPane._
    //aggiunta dei diversi grafici
    pages += new Page("Waiting" ,
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("In attesa della simulazione..")
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

  override def top: Frame = new MainFrame {
    title = "PPS-19-Covid-Sim"

    //TextBoxes
    val probInfectionField = new TextField(3)
    probInfectionField.text = "70"
    val minHealingTimingField = new TextField(4)
    minHealingTimingField.text = "7"
    val maxHealingTimingField = new TextField(4)
    maxHealingTimingField.text = "40"
    val minInfectionDetectionTimeField = new TextField(4)
    minInfectionDetectionTimeField.text = "3"
    val maxInfectionDetectionTimeField = new TextField(4)
    maxInfectionDetectionTimeField.text = "1"
    val multipleInfectionProbabilityField = new TextField(3)
    multipleInfectionProbabilityField.text = "10"
    //val asymptomaticAgeField = new TextField(3)
    //TODO capire come gestire questo
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
    val monthStartField = new TextField(2)
    val yearStartField = new TextField(4)
    val dayEndField = new TextField(2)
    val monthEndField = new TextField(2)
    val yearEndField = new TextField(4)

    //runs
    val runsField = new TextField(3)
    runsField.text = "1"

    dayStartField.text = "1"
    monthStartField.text = "1"
    yearStartField.text = "2020"
    dayEndField.text = "1"
    monthEndField.text = "12"
    yearEndField.text = "2020"


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
    val fieldCheckbox = new CheckBox("Campi(Calcio)")
    val gymCheckbox = new CheckBox("Palestre")

    /*Separare i radio e gestire la selezione
      contents += new BoxPanel(Orientation.Vertical) {
      border = CompoundBorder(TitledBorder(EtchedBorder, "Radio Buttons"), EmptyBorder(5, 5, 5, 10))
      val a = new RadioButton("Green Vegetables")
      val b = new RadioButton("Red Meat")
      val c = new RadioButton("White Tofu")
      val mutex = new ButtonGroup(a, b, c)
      contents ++= mutex.buttons
    }*/

    /*
     * Create a menu bar with a couple of menus and menu items and
     * set the result as this frame's menu bar. TODO chose what to show in the menu.. maybe we can give the user the possibility to download the simulation data and maybe give a functionality that allow to load a graph from a file
     */
    menuBar = new MenuBar {
      contents += new Menu("File") {
        //probably if we want to use these components we should declare them out of this method
        contents += new MenuItem(Action("Save Simulation") {
          println("Action '" + title + "' invoked")
        })
        contents += new MenuItem(Action("Load Simulation") {
          println("Action '" + title + "' invoked")
        })
      }
      contents += new Menu("Another example Menu") {
        contents += new MenuItem("An item")
        contents += new MenuItem(Action("An action item") {
          println("Action '" + title + "' invoked")
        })
        contents += new Separator
        contents += new CheckMenuItem("Check me")
        contents += new CheckMenuItem("Me too!")
        contents += new Separator
        val a = new RadioMenuItem("a")
        val b = new RadioMenuItem("b")
        val c = new RadioMenuItem("c")
        val mutex = new ButtonGroup(a, b, c)
        contents ++= mutex.buttons
      }
      //another menu if needed
      contents += new Menu("Empty Menu")
    }

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

        /**
         * Method that check if the inserted value is a number between 0 and 100
         * @param textField containing the value to check
         */
        def checkPercent(textField: TextField): Unit = {
          if (textField.text.forall(c => c.isDigit) && textField.text!="") {
            if (textField.text.toInt > 100) {textField.text = "100"}
            else if (textField.text.toInt < 0) {textField.text = "0"}
          } else {textField.text = "0"}
        }

        /**
         * Method that check if the inserted value is a number positive number
         * @param textField containing the value to check
         */
        def checkPositive(textField: TextField): Unit = {
          if (textField.text.forall(c => c.isDigit) && textField.text!="") {
            if (textField.text.toInt < 0) {textField.text = "0"}
          } else {textField.text = "0"}
        }

        def checkDay(textField: TextField): Unit = {
          if (textField.text.forall(c => c.isDigit) && textField.text!="") {
            if (textField.text.toInt < 0) {textField.text = "1"}
            if (textField.text.toInt >31) {textField.text = "0"}
          } else {textField.text = "1"}
        }
        def checkMonth(textField: TextField): Unit = {
          if (textField.text.forall(c => c.isDigit) && textField.text!="") {
            if (textField.text.toInt < 0) {textField.text = "1"}
            if (textField.text.toInt > 12) {textField.text = "12"}
          } else {textField.text = "1"}
        }

        val comboboxItems = Seq("gigi","el1","el1","el1","el1","el1","el1","el1","el1")

        val comboBox: ComboBox[String]= new ComboBox[String](comboboxItems)
        contents += new FlowPanel {
          contents += new Label("<html><p>Nazione, Regione o Provincia su cui Simulare:</p></html>")//contagionProbability
          //val probInfectionField = new TextField(3)
          contents += comboBox
          comboBox.selection.item
          contents += new Label("   Runs:")
          contents += runsField
          listenTo(runsField)
          reactions += {
            case EditDone(`runsField`) => checkPositive(runsField)
          }



          //println(comboBox.selection.item)
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
          contents += new Label("<html><p>tempo minimo di guarigione dal virus:</p></html>")//minInfectionDetectionTime
          contents += minInfectionDetectionTimeField
          contents += new Label("Giorni")
          listenTo(minInfectionDetectionTimeField)
          reactions += {
            case EditDone(`minInfectionDetectionTimeField`) => checkPositive(minInfectionDetectionTimeField)
          }
        }

        contents += new FlowPanel {
          contents += new Label("<html><p>tempo massimo di guarigione dal virus:</p></html>")//maxInfectionDetectionTime
          contents += maxInfectionDetectionTimeField
          contents += new Label("Giorni")
          listenTo(maxInfectionDetectionTimeField)
          reactions += {
            case EditDone(`maxInfectionDetectionTimeField`) => checkPositive(maxInfectionDetectionTimeField)
          }
        }


        /*contents += new FlowPanel {
          contents += new Label("<html><p>Percentuale stimata di asintomatici per facie di età:</p></html>")//TODO asymptomaticProbability   gli assegnerei poi => valore fisso frega dell'età
          //val asymptomaticAgeField = new TextField(3)
          contents += asymptomaticAgeField
          contents += new Label("%")
          listenTo(asymptomaticAgeField)
          reactions += {
            case EditDone(`asymptomaticAgeField`) => checkPercent(asymptomaticAgeField)
          }
        }*/

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

        contents += new BoxPanel(Orientation.Vertical) {
          border = CompoundBorder(TitledBorder(EtchedBorder, "Posti chiusi nel LockDown"), EmptyBorder(5, 5, 5, 10))
          //contents += new Label("Quali locali e strutture chiudere")
          contents ++= Seq(beachCheckbox,squareCheckbox,parkCheckbox,resturantCheckbox, pubCheckbox, barCheckbox, discoCheckbox,openDiscoCheckbox,schoolCheckbox, universityCheckBox,companyCheckbox,factoryCheckbox , shopCheckbox, fieldCheckbox,gymCheckbox)
        }

        contents +=  new BorderPanel {
          val confirmButton = new Button("Conferma")
          add(confirmButton, BorderPanel.Position.East)
          listenTo(confirmButton)
          reactions += {
            case ButtonClicked(`confirmButton`) => {
              //check che i valori obbligatori siano inseriti
              //valori: beachCheckbox,squareCheckbox,parkCheckbox,resturantCheckbox, pubCheckbox, barCheckbox, discoCheckbox,openDiscoCheckbox,schoolCheckbox, universityCheckBox,companyCheckbox,factoryCheckbox , shopCheckbox, fieldCheckbox,gymCheckbox
              //li usi per fare partire la simulazione
              println("confirm!!!!")
              //tabs.pages += new Page("Painting", btnPanel.buttons )
              var dataInizio: Calendar = ScalaCalendar(yearStartField.text.toInt,monthStartField.text.toInt,dayStartField.text.toInt,0)
              var dataFine: Calendar = ScalaCalendar(yearEndField.text.toInt,monthEndField.text.toInt,dayEndField.text.toInt,0)
              //if(dataInizio.isBefore(dataFine))//TODO non è stato implementato un metodo che lo faccia

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
                lockdownEndField.text.toDouble/100)


                controller.startSimulation(null, dataInizio,dataFine,runsField.text.toInt) // TODO: specificare l'area (es. città o regione...)
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
}

//TODO TO REMOVE IN FUTURE
object btnPanel {
  val buttons: FlowPanel = new FlowPanel {
    border = Swing.EmptyBorder(5, 5, 5, 5)

    contents += new BoxPanel(Orientation.Vertical) {
      border = CompoundBorder(TitledBorder(EtchedBorder, "Radio Buttons"), EmptyBorder(5, 5, 5, 10))
      val a = new RadioButton("Green Vegetables")
      val b = new RadioButton("Red Meat")
      val c = new RadioButton("White Tofu")
      val mutex = new ButtonGroup(a, b, c)
      contents ++= mutex.buttons
    }
    contents += new BoxPanel(Orientation.Vertical) {
      border = CompoundBorder(TitledBorder(EtchedBorder, "Check Boxes"), EmptyBorder(5, 5, 5, 10))
      val paintLabels = new CheckBox("Paint Labels")
      val paintTicks = new CheckBox("Paint Ticks")
      val snapTicks = new CheckBox("Snap To Ticks")
      val live = new CheckBox("Live")
      contents ++= Seq(paintLabels, paintTicks, snapTicks)
      listenTo(paintLabels, paintTicks, snapTicks)
      reactions += {
        case ButtonClicked(`paintLabels`) =>
          println("paintLabels clicked")
        case ButtonClicked(`paintTicks`)=>
          println("paintTicks clicked")
        case ButtonClicked(`snapTicks`) =>
          println("snapTicks clicked")
      }
    }
  }
}
