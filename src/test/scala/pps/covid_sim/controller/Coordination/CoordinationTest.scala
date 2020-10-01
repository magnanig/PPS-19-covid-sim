package pps.covid_sim.controller.Coordination

import java.util.Calendar

import org.junit.Test
import pps.covid_sim.controller.ControllerImpl
import pps.covid_sim.controller.actors.ActorsCoordination
import pps.covid_sim.model.people.People.Student
import pps.covid_sim.model.people.Person

import scala.collection.parallel.ParSeq
import pps.covid_sim.model.places.{Habitation, Locality}
import pps.covid_sim.model.places.Locality.{City}
import pps.covid_sim.model.places.Locality
import pps.covid_sim.model.places.Locality.{Province, Region}
import pps.covid_sim.model.samples.Provinces
import pps.covid_sim.model.simulation.Simulation
import pps.covid_sim.view.GuiImp
//import pps.covid_sim.people.actors.Communication.{Acknowledge, Tick}
import pps.covid_sim.util.time.DatesInterval
import pps.covid_sim.util.time.Time.ScalaCalendar


class CoordinationTest {


  @Test
  def testCoordinationWithActors(): Unit = {
    val c = new ControllerImpl
    val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
    val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

    val interval = new DatesInterval(start,end)
    ActorsCoordination.create(c,interval)
  }


  /*@Test
  def testIt(): Unit = {
    case class Init(controller: ControllerImpl, datesInterval: DatesInterval)
    val c = new ControllerImpl() {
      override def startSimulation(from: Calendar, until: Calendar, runs: Int): Unit = ???

      override def tick(time: Calendar): Unit = ???

      override def simulationEnded(simulation: Simulation): Unit = {}//TODO

      override def startLockdown(time: Calendar, infections: Int): Unit = ???

      override def endLockdown(time: Calendar, infections: Int): Unit = ???

      override def regions: Seq[Region] = Seq(Region.EMILIA_ROMAGNA,Region.FRIULI_VENEZIA_GIULIA)

      override def provinces: Seq[Province] = Seq(Province.BOLOGNA, Province.MILANO, Province.ROMA, Province.TORINO)

      override def people: Seq[Person] = Seq(
        Student(ScalaCalendar(1997,1,1,1),Locality.City(1,"Bologna",1,Province.BOLOGNA)),
        Student(ScalaCalendar(1997,1,1,1),Locality.City(2,"Milano",1,Province.MILANO)),
        Student(ScalaCalendar(1997,1,1,1),Locality.City(3,"Roma",1,Province.ROMA)),
        Student(ScalaCalendar(1997,1,1,1),Locality.City(4,"Torino",1,Province.TORINO)),
      )
    }
    val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
    val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

    val interval = new DatesInterval(start,end)
    // Wrap the test procedure within a testkit constructor
    // if you want to receive actor replies or use Within () , etc.
    val system = ActorSystem.create()

    new TestKit (system) {
      val subject:ActorRef = system.actorOf(Props[ActorsCoordinator]);
      val osu :ActorRef = system.actorOf(Props[RegionCoordinator]);
      val probe: TestKit = new TestKit ( system ); // can also use it ’from the outside ’
      osu ! Acknowledge()
      probe.expectMsg(FiniteDuration(1,"sec"), Init(c, interval)); // await the correct response
      subject ! Init(c, interval)
      probe.expectMsg(FiniteDuration(1,"sec"), Init(c, interval)); // await the correct response
      probe.expectMsg(FiniteDuration(1,"sec"), Acknowledge()); // await the correct response

    };
  }*/


}



object TestingCoordination extends App {
  val c = new ControllerImpl() {
    override def startSimulation(from: Calendar, until: Calendar, runs: Int): Unit = ???

    override def tick(time: Calendar): Unit = ???

    override def simulationEnded(simulation: Simulation): Unit = {}//TODO

    override def startLockdown(time: Calendar, infections: Int): Unit = ???

    override def endLockdown(time: Calendar, infections: Int): Unit = ???

    override def regions: Seq[Region] = Seq(Region.EMILIA_ROMAGNA,Region.FRIULI_VENEZIA_GIULIA)

    override def provinces: Seq[Province] = Seq(Provinces.BOLOGNA, Provinces.MILANO, Provinces.ROMA, Provinces.TORINO)


    val p1 = Student(ScalaCalendar(1997,1,1,1),Locality.City(1,"Bologna",1,Provinces.BOLOGNA))
    val a1 = Habitation(City(1,"inventata",1,Provinces.BOLOGNA))
    p1.setHabitation(a1)
    a1.addMember(p1)
    val p2 = Student(ScalaCalendar(1997,1,1,1),Locality.City(2,"Milano",1,Provinces.MILANO))
    val a2 = Habitation(City(2,"inventata",1,Provinces.MILANO))
    p2.setHabitation(a2)
    a2.addMember(p2)
    val p3 = Student(ScalaCalendar(1997,1,1,1),Locality.City(3,"Roma",1,Provinces.ROMA))
    val a3 = Habitation(City(3,"inventata",1,Provinces.ROMA))
    p3.setHabitation(a3)
    a3.addMember(p3)
    val p4 = Student(ScalaCalendar(1997,1,1,1),Locality.City(4,"Torino",1,Provinces.TORINO))
    val a4 = Habitation(City(4,"inventata",1,Provinces.TORINO))
    p4.setHabitation(a4)
    a4.addMember(p4)

    override def people: ParSeq[Person] = ParSeq(p1,p2,p3,p4)
  }
  val start: Calendar = ScalaCalendar(2020, 9, 1, 1)
  val end: Calendar = ScalaCalendar(2020, 9, 1, 10)

  val interval = new DatesInterval(start,end)
  ActorsCoordination.create(c,interval)
}
