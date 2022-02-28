import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-LUCENESEARCHENGINE"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file ("."))
  .settings (
    name := "LuceneSearchEngine",
    libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "8.11.1",
    libraryDependencies += "org.apache.lucene" % "lucene-core" % "8.11.1",
    libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "8.11.1",
      libraryDependencies += "org.apache.lucene" % "lucene-highlighter" % "8.11.1",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0",
    libraryDependencies += "commons-io" % "commons-io" % "2.11.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
  )