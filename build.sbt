name := """tefnut"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-test" % "2.2.6" % "test",
  "org.scalikejdbc" %% "scalikejdbc-play-plugin" % "2.3.6",
  "org.scalikejdbc" %% "scalikejdbc-play-fixture-plugin" % "2.3.6",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.8",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.apache.httpcomponents" % "httpclient" % "4.4.1",
  "org.json4s" %% "json4s-core" % "3.2.9",
  "org.json4s" %% "json4s-native" % "3.2.9",
  "com.github.tototoshi"  %%  "play-flyway"  %  "1.1.3",
  "com.typesafe.scala-logging"  %%  "scala-logging"  %  "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback"  %  "logback-classic"  %  "1.1.2",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.3.0-akka-2.3.x"
)
