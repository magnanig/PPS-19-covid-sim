package pps.covid_sim.model.people

object PeopleGroup {

  implicit def personToGroup(person: Person): Group = Single(person)

  trait Group extends Iterable[Person] {

    def leader: Person

    def people: Set[Person]

    def +(group: Group): Group = Multiple(leader, people ++ group.people)

    override def iterator: Iterator[Person] = people.iterator

  }

  object Group {
    def ofPeople(leader: Person, people: Person*): Group = {
      if(people.isEmpty) Single(leader)
      else Multiple(leader, people.toSet + leader)
    }
  }

  case class Single(leader: Person) extends Group {
    override def people: Set[Person] = Set(leader)
  }

  case class Multiple(leader: Person, people: Set[Person]) extends Group {
    require(people.contains(leader) && people.size > 1)
  }

}
