ThisBuild / useCoursier := false // to solve some cache problems (from https://stackoverflow.com/a/58456468)

name := "covid-sim"
version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

libraryDependencies += "org.jfree" % "jfreechart" % "1.5.0"

val AkkaVersion = "2.6.8"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"
