package pps.covid_sim.view

import scala.swing.ListView._
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

object UserInterface extends SimpleSwingApplication {
  def top: Frame = new MainFrame {
    title = "PPS-19-Covid-Sim"

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

    /*
     * The root component in this frame is a panel with a border layout.
     */
    contents = new BorderPanel {

      import BorderPanel.Position._

      val tabs: TabbedPane = new TabbedPane {

        import TabbedPane._

        //aggiunta dei diversi grafici
        pages += new Page("Graph 1" ,
          new BoxPanel(Orientation.Vertical) {
            contents += new Label("Graph1")
          })
        pages += new Page("Graph 2" ,
          new BoxPanel(Orientation.Vertical) {
            contents += new Label("Graph2")
          })
        pages += new Page("Graph 3" ,
          new BoxPanel(Orientation.Vertical) {
            contents += new Label("Graph3")
          })



        pages += new Page("Buttons"     , btnPanel.buttons)
        pages += new Page("GridBag"     , btnPanel.buttons)
        pages += new Page("Converter"   , btnPanel.buttons)
        pages += new Page("Tables"      , btnPanel.buttons)
        pages += new Page("Dialogs"     , btnPanel.buttons)
        pages += new Page("Combo Boxes" , btnPanel.buttons)
        pages += new Page("Split Panes" ,
          new SplitPane(Orientation.Vertical, new Button("Hello"), new Button("World")) {
            continuousLayout = true
          })

        val password: FlowPanel = new FlowPanel {
          contents += new Label("Enter your secret password here ")
          val field = new PasswordField(10)
          contents += field
          val label = new Label(field.text)
          contents += label
          listenTo(field)
          reactions += {
            case EditDone(`field`) => label.text = field.password.mkString
          }
        }

        pages += new Page("Password", password, "Password tooltip")
        pages += new Page("Painting", btnPanel.buttons )
        //pages += new Page("Text Editor", TextEditor.ui)
      }

      val list: ListView[TabbedPane.Page] = new ListView(tabs.pages) {
        selectIndices(0)
        selection.intervalMode = ListView.IntervalMode.Single
        renderer = ListView.Renderer(_.title)
      }

      /**
       * this panel contains the form that allow the user to insert specific parameters for the simulation
       * - probabilità contagio al netto di mascherine e distanziamento sociale
       * - tempo minio e massimo di guarigione dal virus
       * - percentuale stimata di asintomatici per facie di età
       * - percentuale di asintomatici che riescono ad accorgiersi di essere infetti
       * - distanza ad di sopra della quale è garantito che il virus non può transitare da persona a persona
       */
      val leftPanel: BoxPanel = new BoxPanel(Orientation.Vertical){
        contents += new BoxPanel(Orientation.Vertical) {
          border = CompoundBorder(TitledBorder(EtchedBorder, "Radio Buttons"), EmptyBorder(5, 5, 5, 10))
          val a = new RadioButton("Radio1")
          val b = new RadioButton("Radio2")
          val c = new RadioButton("Radio3")
          val mutex = new ButtonGroup(a, b, c)
          contents ++= mutex.buttons
        }
        contents += new BoxPanel(Orientation.Vertical) {
          border = CompoundBorder(TitledBorder(EtchedBorder, "Check Boxes"), EmptyBorder(5, 5, 5, 10))

          val checkbox1 = new CheckBox("checkbox1")
          val checkbox2 = new CheckBox("checkbox2")
          val checkbox3 = new CheckBox("checkbox3")

          val paintLabels = new CheckBox("Paint Labels")
          val paintTicks = new CheckBox("Paint Ticks")
          val snapTicks = new CheckBox("Snap To Ticks")
          val live = new CheckBox("Live")
          contents ++= Seq(checkbox1,checkbox2,checkbox3,paintLabels, paintTicks, snapTicks)
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

      val center: SplitPane = new SplitPane(Orientation.Vertical, leftPanel, tabs) {//qui ci metteremo i grafici nelle tabs
        oneTouchExpandable = true
        continuousLayout = true
      }
      layout(center) = Center

      /*
       * Establish connection between the tab pane, slider, and list view.
       */
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



