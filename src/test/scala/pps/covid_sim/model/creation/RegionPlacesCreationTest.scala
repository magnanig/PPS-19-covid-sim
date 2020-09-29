package pps.covid_sim.model.creation

import org.junit.Test
import org.junit.Assert.assertEquals
import pps.covid_sim.model.container.{CitiesContainer, PeopleContainer, PlacesContainer}
import pps.covid_sim.model.people.People.Worker
import pps.covid_sim.model.places.FreeTime.{Bar, Disco, OpenDisco, Pub, Restaurant}
import pps.covid_sim.model.places.Hobbies.{FootballTeam, Gym}
import pps.covid_sim.model.places.Jobs.{Company, Factory}
import pps.covid_sim.model.places.Locality.Region
import pps.covid_sim.model.places.OpenPlaces.{Field, Park, Square}
import pps.covid_sim.model.places.Shops.Shop

import scala.collection.mutable

class RegionPlacesCreationTest {

  RegionPlacesCreation.create(Region.VALLE_DAOSTA)

  val squares: Int = PlacesContainer.getPlaces(classOf[Square]).size
  //val schools: List[School] = PlacesContainer.getPlaces(classOf[School]).map(_.asInstanceOf[School])
  //val universities: List[University] = PlacesContainer.getPlaces(classOf[University]).map(_.asInstanceOf[University])
  val companies: List[Company] = PlacesContainer.getPlaces(classOf[Company]).map(_.asInstanceOf[Company])
  val factories: List[Factory] = PlacesContainer.getPlaces(classOf[Factory]).map(_.asInstanceOf[Factory])
  val pubs: List[Pub] = PlacesContainer.getPlaces(classOf[Pub]).map(_.asInstanceOf[Pub])

  /**
   * Questo test certifica che tutti gli studenti di una provincia
   * vengono tutti assegnati correttamente ad un istituto scolastico.
   */
  @Test
  def testSchoolCreation(): Unit = {

    /*val students: List[Student] = PeopleCreation.getPeople.filter(_.residence == Cities.AOSTA).filter(_.getClass == classOf[Student]).map(_.asInstanceOf[Student])
    val teachers: List[Teacher] = PeopleCreation.getPeople.filter(_.residence == Cities.AOSTA).filter(_.getClass == classOf[Teacher]).map(_.asInstanceOf[Teacher])

    val studentsAssigned: Int = students.count(student => student.institute != null)
    val teachersAssigned: Int = teachers.count(teacher => teacher.workPlace != null)

    assertEquals(students.size, studentsAssigned)
    assertEquals(teachers.size, teachersAssigned)*/

  }

  /**
   * Verifichiamo che in PlacesContainer siano presenti tutti i Pub
   * che sono stati come luogo di lavoro a diversi lavoratori
   *
   * C’è un test che, dati N lavoratori di pub, si verifica che nella
   * getPlaces ci siano esattamente tutti i pub nei quali le N persone
   * lavorano? Perché per quello che sto vedendo non sembra..
   * Per azzerare tutto non basta che faccio getPlaces.foreach.(reset),
   * ma oltre a questo devo andare da ogni worker, prendere il suo posto
   * dove lavora e richiamare reset su quello. Ma se quell’oggetto fosse
   * già in getPlaces non dovrei ripeterlo così a mano..
   * Per quello dico così...
   */
  @Test
  def testPubAssignedToWorker(): Unit = {

    PeopleContainer.checkAssignedWork()
    // Starting with the workers, I get the pubs where the workers work
    val workers: List[Worker] = PeopleContainer.getPeople.filter(_.getClass == classOf[Worker]).map(_.asInstanceOf[Worker])
    val workerPubs: mutable.Set[Pub] = mutable.Set()
    workers.map(_.workPlace).filter(_.getClass == classOf[Pub])
      .map(_.asInstanceOf[Pub]).foreach(pub => if (!workerPubs.contains(pub)) workerPubs += pub)

    // Is it possible to match the two values present in this test?
    assertEquals(workerPubs.size, pubs.size)

    println("\nPub che sono stati assegnati ai lavoratori: " + workerPubs.size)
    println("Pub restituiti da PlacesContainer: " + pubs.size)

    workerPubs.foreach(pub => assert(pubs.contains(pub)))
    pubs.foreach(pub => assert(workerPubs.contains(pub)))

  }

  /**
   * This test certifies that each company or factory has been assigned
   * a number of workers equal to or less than the capacity of the
   * company or factory. Therefore, there are no companies to which
   * a number of workers greater than the capacity of the company
   * have been assigned
   */
  @Test
  def testWorkerAssignmentToOfficesRun(): Unit = {
    companies.foreach(company => assert(company.capacity >= company.workPlans.keySet.size))
    factories.foreach(factory => assert(factory.capacity >= factory.workPlans.keySet.size))
  }

  @Test
  def testPlacesCreation(): Unit = {
    assertEquals(CitiesContainer.getCities.size, squares)
    println("Numero di Piazze create: " + squares)
    println("Numero di Parchi creati: " + PlacesContainer.getPlaces(classOf[Park]).size)
    println("Numero di Campi da calcio creati: " + PlacesContainer.getPlaces(classOf[Field]).size + "\n")

    //println("Numero di Scuole create: " + schools.size)
    //println("Numero di Università create: " + universities.size + "\n")

    println("Numero di Aziende create: " + companies.size)
    println("Numero di Fabbriche create: " + factories.size + "\n")

    println("Numero di Negozi creati: " + PlacesContainer.getPlaces(classOf[Shop]).size + "\n")

    println("Numero di FootballTeam creati: " + PlacesContainer.getPlaces(classOf[FootballTeam]).size)
    println("Numero di Palestre create: " + PlacesContainer.getPlaces(classOf[Gym]).size + "\n")

    println("Numero di Ristoranti creati: " + PlacesContainer.getPlaces(classOf[Restaurant]).size)
    println("Numero di Bar creati: " + PlacesContainer.getPlaces(classOf[Bar]).size)
    println("Numero di Pub creati: " + pubs.size)
    println("Numero di Disco create: " + PlacesContainer.getPlaces(classOf[Disco]).size)
    println("Numero di Open Disco create: " + PlacesContainer.getPlaces(classOf[OpenDisco]).size + "\n")
  }

}
