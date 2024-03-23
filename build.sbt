val scala3Version = "3.4.0"
val jqfs2Version = "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "jq-examples",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "1.0.0-M40",
      "org.http4s" %% "http4s-dsl" % "1.0.0-M40",
      "org.http4s" %% "http4s-circe" % "1.0.0-M40",
      "co.fs2" %% "fs2-core" % "3.8.0",
      "co.fs2" %% "fs2-io" % "3.8.0",
      "io.circe" %% "circe-parser" % "0.15.0-M1",
      "io.circe" %% "circe-generic" % "0.15.0-M1",
      "org.slf4j" % "slf4j-nop" % "2.0.9",
      "io.circe" %% "circe-optics" % "0.15.0",
      "dev.optics" %% "monocle-macro" % "3.2.0",
      "jqscala" %% "jq-fs2" % jqfs2Version
    )
      
  )
