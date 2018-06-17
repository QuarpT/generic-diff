name := "generic-diff"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

crossScalaVersions := Seq("2.11.11", "2.12.5")

version := {
  val versionPrefix = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => "1"
    case _ => "2"
  }
  s"$versionPrefix.0.0"
}

ghreleaseRepoOrg := "QuarpT"
ghreleaseRepoName := "generic-diff"
