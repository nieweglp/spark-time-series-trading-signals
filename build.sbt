ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.13"
assembly / mainClass := Some("org.tss.application")

lazy val root = (project in file("."))
  .settings(
    name := "tss",
    idePackagePrefix := Some("org.tss.application")
  )

val sparkVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe.play" %% "play-json" % "2.10.0",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "com.holdenkarau" %% "spark-testing-base" % "2.4.7_0.14.0" % Test
)
