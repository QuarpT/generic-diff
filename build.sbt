name := "generic-diff"

version := "0.1.0"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)
