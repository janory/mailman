import sbt._

object Dependencies {

  private lazy val akkaHttpVersion      = "10.1.4"
  private lazy val akkaVersion          = "2.5.15"
  private lazy val akkaHttpCirceVersion = "1.20.0-RC1"
  private lazy val ficusVersion         = "1.4.2"
  private lazy val logbackVersion       = "1.2.1"
  private lazy val scalaLoggingVersion  = "3.5.0"
  private lazy val slf4jVersion         = "1.7.23"
  private lazy val circeVersion         = "0.8.0"
  private lazy val scalaTestVersion     = "3.0.5"
  private lazy val gatlingVersion       = "2.3.1"

  private lazy val akkaHttp             = "com.typesafe.akka"          %% "akka-http"                % akkaHttpVersion
  private lazy val akkaStream           = "com.typesafe.akka"          %% "akka-stream"              % akkaVersion
  private lazy val akkaHttpCirce        = "de.heikoseeberger"          %% "akka-http-circe"          % akkaHttpCirceVersion
  private lazy val ficus                = "com.iheart"                 %% "ficus"                    % ficusVersion
  private lazy val circeCore            = "io.circe"                   %% "circe-core"               % circeVersion
  private lazy val circeGeneric         = "io.circe"                   %% "circe-generic"            % circeVersion
  private lazy val circeParser          = "io.circe"                   %% "circe-parser"             % circeVersion
  private lazy val circeJava8           = "io.circe"                   %% "circe-java8"              % circeVersion
  private lazy val circeGenericExtrac   = "io.circe"                   %% "circe-generic-extras"     % circeVersion
  private lazy val akkaSlf4j            = "com.typesafe.akka"          %% "akka-slf4j"               % akkaVersion
  private lazy val julToSlf4j           = "org.slf4j"                  % "jul-to-slf4j"              % slf4jVersion
  private lazy val scalaLogging         = "com.typesafe.scala-logging" %% "scala-logging"            % scalaLoggingVersion
  private lazy val logbackClassic       = "ch.qos.logback"             % "logback-classic"           % logbackVersion
  private lazy val akkaHttpTestkit      = "com.typesafe.akka"          %% "akka-http-testkit"        % akkaHttpVersion % "test,it"
  private lazy val akkaTestkit          = "com.typesafe.akka"          %% "akka-testkit"             % akkaVersion % "test,it"
  private lazy val scalaTest            = "org.scalatest"              %% "scalatest"                % scalaTestVersion % "test,it"
  private lazy val gatlingHighcharts    = "io.gatling.highcharts"      % "gatling-charts-highcharts" % gatlingVersion % "test,it"
  private lazy val gatlingTestFramework = "io.gatling"                 % "gatling-test-framework"    % gatlingVersion % "test,it"

  lazy val mailmanApp = Seq(
    akkaHttp,
    akkaStream,
    akkaHttpCirce,
    ficus,
    circeCore,
    circeGeneric,
    circeParser,
    circeJava8,
    circeGenericExtrac,
    akkaSlf4j,
    julToSlf4j,
    scalaLogging,
    logbackClassic,
    akkaHttpTestkit,
    akkaTestkit,
    scalaTest
  )

  lazy val mailmanAppLoadtest = Seq(
    julToSlf4j,
    scalaLogging,
    logbackClassic,
    gatlingHighcharts,
    gatlingTestFramework
  )

}
