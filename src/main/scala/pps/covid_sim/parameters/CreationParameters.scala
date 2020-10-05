package pps.covid_sim.parameters

object CreationParameters {

  val citizensPercentage = 0.001 //0.25
  val minCitizens = 100

  val oneHundredPercent = 100.0
  val population = 60405646
  val daysInYear = 365
  val minResidencesToSchool = 3000
  val studentsPerClass = 30
  val studentsUniPerClass = 100
  val classesPerSchool = 30
  val classesPerUni = 15

  // the sum must be equal to 100
  val schoolStudentsPercentage = 80.0
  val uniStudentsPercentage = 20.0

  // the sum must be equal to 100
  val schoolTeachersPercentage = 80.0
  val uniTeachersPercentage = 20.0

  // the sum must be equal to 100
  val workersPercentage = 60.0
  val teachersPercentage = 5.0
  val studentsPercentage = 15.0
  val unemployedPercentage = 20.0

  // the sum must be equal to 100
  val companyPercentage = 26.0
  val factoryPercentage = 26.0
  val shopPercentage = 18.0
  val hobbyPercentage = 10.0
  val freeTimePlacePercentage = 20.0

  // the sum must be equal to 100
  val superMarketPercentage = 30.0
  val clothesShopPercentage = 70.0

  // the sum must be equal to 100
  val gymPercentage = 40.0
  val footballTeamPercentage = 60.0

  // the sum must be equal to 100
  val restaurantsPercentage = 38.0
  val barPercentage = 38.0
  val pubPercentage = 10.0
  val discoPercentage = 10.0
  val openDiscoPercentage = 4.0

  val teacherUniversityPercentage = 5 // un quinto dei teacher totali
  val studentUniversityPercentage = 5 // un quinto degli student totali

  val maxNumShopPerWeek = 5

  // Obstacles generation parameters for the different places
  val minNumDiscoObstacles = 1
  val maxNumDiscoObstacles = 4
  val minNumPubObstacles = 1
  val maxNumPubObstacles = 4
  val minNumOpenDiscoObstacles = 1
  val maxNumOpenDiscoObstacles = 4

  val minSquareObstaclesFactor = 3000 // an obstacle will be inserted on average every 2000 square meters
  val maxSquareObstaclesFactor = 2000
  val minParkObstaclesFactor = 1000
  val maxParkObstaclesFactor = 500
  val minGymObstaclesFactor = 10
  val maxGymObstaclesFactor = 6

  val beachFillFactor = 4
  val supermarketFillFactor = 2
  val clothesShopFillFactor = 2

}
