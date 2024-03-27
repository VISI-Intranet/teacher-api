ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "TeacherMicro" ,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.16",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0",
      "de.heikoseeberger" %% "akka-http-json4s" % "1.37.0",
      "org.json4s" %% "json4s-native" % "3.6.11",
      "org.json4s" %% "json4s-jackson" % "3.6.11",
      "com.typesafe.akka" %% "akka-actor" % "2.6.16",
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      "io.spray" %% "spray-json" % "1.3.6",
      "org.slf4j" % "slf4j-api" % "1.7.32",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.rabbitmq" % "amqp-client" % "5.14.0",
      "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "6.0.2",
      "com.typesafe.play" %% "play-json" % "2.9.2",
      "io.argonaut" %% "argonaut" % "6.3.4"


    )
  )
