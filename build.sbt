//ThisBuild / useCoursier := false // to solve some cache problems (from https://stackoverflow.com/a/58456468)

name := "covid-sim"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

// https://mvnrepository.com/artifact/org.scalanlp/breeze-viz
libraryDependencies += "org.scalanlp" %% "breeze-viz" % "1.0"
