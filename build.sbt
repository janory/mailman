import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtTestOnCompile
import sbt.Defaults
import sbt.Keys.libraryDependencies

lazy val `mailman` = (project in file("."))
  .aggregate(`mailman-app`)
  .aggregate(`mailman-app-loadtest`)
  .settings(
    inThisBuild(
      List(
        organization := "com.janory",
        scalaVersion := "2.12.6"
      )
    ),
    name := "mailman"
  )

lazy val `mailman-app-loadtest` = (project in file("mailman-app-loadtest"))
  .enablePlugins(GatlingPlugin)
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Dependencies.mailmanAppLoadtest
  )

lazy val `mailman-app` = (project in file("mailman-app"))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Dependencies.mailmanApp,
    javaOptions in run += "-Xmx128M"
  )

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8"
  ),
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8"
  ),
  scalafmtOnCompile := true,
  scalafmtTestOnCompile := true,
  fork in run := true,
  fork in Test := true,
  fork in IntegrationTest := true,
  parallelExecution in IntegrationTest := false
)
