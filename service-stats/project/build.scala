import sbt._
import sbt.Keys._
import Dependencies._

object AppBuilder extends Build {

  val appSettings = Seq(
    name := "service-stats",
    organization := "io.github.rlazoti",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-language:postfixOps", "-encoding", "utf8"),

    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
    unmanagedSourceDirectories in Test    <<= (scalaSource in Test)(Seq(_))
  )

  lazy val app = Project("service-stats", file("."))
    .settings(appSettings : _*)
    .settings(libraryDependencies ++= appDependencies)

}
