package pps.covid_sim.model.transports

import java.util.Calendar

import org.junit.Assert._
import org.junit.Test
import pps.covid_sim.model.people.People.{Student, Worker}
import pps.covid_sim.model.people.PeopleGroup.{Group, Multiple, Single}
import pps.covid_sim.model.people.Person
import pps.covid_sim.model.places.Locality.City
import pps.covid_sim.model.samples.Cities
import pps.covid_sim.model.transports.PrivateTransports.Car
import pps.covid_sim.model.transports.PublicTransports.{Bus, Train}
import pps.covid_sim.util.RandomGeneration.randomBirthDate
import pps.covid_sim.util.time.Time.ScalaCalendar

class TransportsTest {

  val cityTest: City = Cities.FORLI

  val bus: Bus = Bus(3)
  val train: Train = Train(2)
  val car: Car = Car(2)

  var people: Seq[Person] = (0 to 40).map(_ => Worker(randomBirthDate(18, 70), cityTest))

  val commuters: Seq[Single] = (1 to 40).map(i => Single(people(i))).toList

  val groupCommuters: Seq[Group] = (1 to 39 by 2).map(s => Multiple(people(s),
    Set(people(s), people(s + 1)))).toList

  val marco: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val lorenzo: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val gianmarco: Single = Single(Student(randomBirthDate(6, 24), cityTest))
  val nicolas: Single = Single(Student(randomBirthDate(6, 24), cityTest))

  val time: Calendar = ScalaCalendar(2020, 9, 1, 15)

  @Test
  def testBusTurnout(): Unit = {
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(nicolas, time))
    // The bus is full
    assertEquals(None, bus.enter(lorenzo, time))
  }

  @Test
  def testBusGroupTurnout(): Unit = {
    assertEquals(Option(bus), bus.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The bus has only one seat available: the group cannot enter
    assertEquals(None, bus.enter(Multiple(people(3),
      Set(people(3), people(4))), time))
    assertEquals(2, bus.numCurrentPeople)
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(3, bus.numCurrentPeople)
    // A person entered in group cannot exit alone
    bus.exit(Single(people(1)))
    assertEquals(3, bus.numCurrentPeople)
    // A group that has not entered together cannot exit together
    bus.exit(Multiple(people(1), Set(people(1), marco.leader)))
    assertEquals(3, bus.numCurrentPeople)
    // Exit of a group
    bus.exit(Multiple(people(1), Set(people(1), people(2))))
    assertEquals(1, bus.numCurrentPeople)
  }

  @Test
  def testExitFromBusWithNoOneInside(): Unit = {
    bus.exit(lorenzo)
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(nicolas, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    // The bus is full
    assertEquals(None, bus.enter(gianmarco, time))
  }

  @Test
  def testDuplicateEntriesInBus(): Unit = {
    assertEquals(Option(bus), bus.enter(marco, time))
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    // Lorenzo is already in: this entry is ignored
    assertEquals(Option(bus), bus.enter(lorenzo, time))
    assertEquals(2, bus.numCurrentPeople)
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    // The bus is full
    assertEquals(None, bus.enter(nicolas, time))
  }

  @Test
  def testDuplicateGroupEntriesInBus(): Unit = {
    assertEquals(Option(bus), bus.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The group has already entered: the operation has no effect
    assertEquals(Option(bus), bus.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    assertEquals(2, bus.numCurrentPeople)
    // The bus has only one seat available: the group cannot enter
    assertEquals(None, bus.enter(Multiple(people(3),
      Set(people(3), people(1))), time))
    assertEquals(2, bus.numCurrentPeople)
    assertEquals(Option(bus), bus.enter(gianmarco, time))
    assertEquals(3, bus.numCurrentPeople)
    // The bus is full
    assertEquals(None, bus.enter(nicolas, time))
    assertEquals(3, bus.numCurrentPeople)
  }

  @Test
  def testCarTurnout(): Unit = {
    assertEquals(Option(car), car.enter(marco, time))
    assertEquals(Option(car), car.enter(lorenzo, time))
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(nicolas, time))
    // The car is full
    assertEquals(None, car.enter(lorenzo, time))
  }

  @Test
  def testCarGroupTurnout(): Unit = {
    assertEquals(Option(car), car.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The car is full
    assertEquals(None, car.enter(marco, time))
    assertEquals(2, car.numCurrentPeople)
    // A person entered in group cannot exit alone
    car.exit(Single(people(1)))
    assertEquals(2, car.numCurrentPeople)
    // A group that has not entered together cannot exit together
    car.exit(Multiple(people(1), Set(people(1), people(3))))
    assertEquals(2, car.numCurrentPeople)
    // Exit of a group
    car.exit(Multiple(people(1), Set(people(1), people(2))))
    assertEquals(0, car.numCurrentPeople)
  }

  @Test
  def testExitFromCarWithNoOneInside(): Unit = {
    car.exit(lorenzo)
    assertEquals(Option(car), car.enter(marco, time))
    assertEquals(Option(car), car.enter(nicolas, time))
    // The car is full
    assertEquals(None, car.enter(gianmarco, time))
  }

  @Test
  def testDuplicateEntriesInCar(): Unit = {
    assertEquals(Option(car), car.enter(lorenzo, time))
    // Lorenzo is already in: this entry is ignored
    assertEquals(Option(car), car.enter(lorenzo, time))
    assertEquals(1, car.numCurrentPeople)
    assertEquals(Option(car), car.enter(gianmarco, time))
    // The car is full
    assertEquals(None, car.enter(nicolas, time))
  }

  @Test
  def testDuplicateGroupEntriesInCar(): Unit = {
    assertEquals(Option(car), car.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The group has already entered: the operation has no effect
    assertEquals(Option(car), car.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The car has no more seats available: the group cannot enter
    assertEquals(None, car.enter(Multiple(people(1),
      Set(people(1), people(3))), time))
    assertEquals(2, car.numCurrentPeople)
  }

  @Test
  def testTrainTurnout(): Unit = {
    enterPeopleFromList(0, commuters.size - 2, commuters, train)
    assertEquals(Option(train), train.enter(marco, time))
    assertEquals(Option(train), train.enter(lorenzo, time))
    train.exit(lorenzo)
    assertEquals(Option(train), train.enter(nicolas, time))
    // The train is full
    assertEquals(None, train.enter(lorenzo, time))
  }

  @Test
  def testTrainGroupTurnout(): Unit = {
    enterPeopleFromList(0, groupCommuters.size - 1, groupCommuters, train)
    assertEquals(Option(train), train.enter(nicolas, time))
    // The train has only one seat available: the group cannot enter
    assertEquals(None, train.enter(Multiple(marco.leader, Set(marco.leader, gianmarco.leader)), time))
    assertEquals(39, train.numCurrentPeople)
    assertEquals(Option(train), train.enter(marco, time))
    // Now the train is full
    assertEquals(40, train.numCurrentPeople)
    // A person entered in group cannot exit alone
    train.exit(Single(people(1)))
    assertEquals(40, train.numCurrentPeople)
    // A group that has not entered together cannot exit together
    train.exit(Multiple(people(3), Set(people(3), people(1))))
    assertEquals(40, train.numCurrentPeople)
    // Exit of a group
    train.exit(Multiple(people(1), Set(people(1), people(2))))
    assertEquals(38, train.numCurrentPeople)
  }

  @Test
  def testExitFromTrainWithNoOneInside(): Unit = {
    train.exit(lorenzo)
    enterPeopleFromList(0, commuters.size - 2, commuters, train)
    assertEquals(Option(train), train.enter(marco, time))
    assertEquals(Option(train), train.enter(lorenzo, time))
    train.exit(marco)
    assertEquals(Option(train), train.enter(marco, time))
  }

  @Test
  def testDuplicateEntriesInTrain(): Unit = {
    assertEquals(Option(train), train.enter(lorenzo, time))
    // Lorenzo is already in: this entry is ignored
    assertEquals(Option(train), train.enter(lorenzo, time))
    assertEquals(1, train.numCurrentPeople)
    assertEquals(Option(train), train.enter(gianmarco, time))
    assertEquals(2, train.numCurrentPeople)
  }

  @Test
  def testDuplicateGroupEntriesInTrain(): Unit = {
    assertEquals(Option(train), train.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    // The group has already entered: the operation has no effect
    assertEquals(Option(train), train.enter(Multiple(people(1),
      Set(people(1), people(2))), time))
    assertEquals(2, train.numCurrentPeople)
  }

  def enterPeopleFromList(from: Int, until: Int, listPeople: Seq[Group], means: Transport ): Unit = {
    listPeople.slice(from, until) foreach(p => means.enter(p, time))
  }

}
