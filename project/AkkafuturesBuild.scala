import sbt._
import sbt.Keys._

object AkkafuturesBuild extends Build {

  lazy val akkafutures = Project(
    id = "akkafutures",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "akkafutures",
      organization := "com.solutionset",
      version := "1.0",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"
    )
  )
}
