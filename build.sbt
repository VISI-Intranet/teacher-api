ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaServicePostgress" ,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.6",
      "com.typesafe.akka" %% "akka-stream" % "2.6.16",
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "org.postgresql" % "postgresql" % "42.2.5",
      "de.heikoseeberger" %% "akka-http-json4s" % "1.37.0",
      // Оставляем только json4s-jackson, так как akka-http-json4s совместим с Jackson
      "org.json4s" %% "json4s-jackson" % "4.0.3",
      "com.typesafe.akka" %% "akka-actor" % "2.6.16",
      "org.apache.kafka" %% "kafka" % "3.4.0",
      "org.apache.kafka" % "kafka-clients" % "3.4.0" ,
      "org.slf4j" % "slf4j-api" % "1.7.32",
      "ch.qos.logback" % "logback-classic" % "1.2.3" ,
      "org.apache.spark" %% "spark-core" % "3.2.0",
      "org.apache.spark" %% "spark-streaming" % "3.2.0",
      "org.apache.spark" %% "spark-sql" % "3.2.0",
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.2.0" ,
      "com.rabbitmq" % "amqp-client" % "5.14.0",
      "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "3.0.4"


    )
  )
