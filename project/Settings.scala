import explicitdeps.ExplicitDepsPlugin.autoImport.{
  moduleFilterRemoveValue,
  unusedCompileDependenciesFilter,
}
import sbt.Keys._
import sbt._
import sbt.nio.Keys.{onChangedBuildSource, ReloadOnSourceChanges}
import sbtide.Keys.idePackagePrefix
import sbtnativeimage.NativeImagePlugin
import sbtnativeimage.NativeImagePlugin.autoImport.{
  nativeImageJvm,
  nativeImageOptions,
  nativeImageVersion,
}
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixSemanticdb
import wartremover.WartRemover.autoImport.{wartremoverErrors, Wart, Warts}

object Settings {
  private[this] val orgPackage: String = "es.eriktorr"
  private[this] val basePackage: String = s"$orgPackage.library"

  def fqClassNameFrom(className: String): Option[String] = Some(
    s"$basePackage.$className",
  )

  def sbtSettings: Seq[Def.Setting[_]] = addCommandAlias(
    "check",
    "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
  )

  private[this] def welcomeMessage: Def.Setting[String] = onLoadMessage := {
    s"""Custom tasks:
       |check - run all project checks
       |""".stripMargin
  }

  private[this] val warts: Seq[wartremover.Wart] = Warts.unsafe.filter(_ != Wart.DefaultArguments)

  private[this] val MUnitFramework = new TestFramework("munit.Framework")

  private[this] def commonSettings(projectName: String): Def.SettingsDefinition = Seq(
    name := projectName,
    resolvers += "Confluent" at "https://packages.confluent.io/maven/",
    ThisBuild / organization := orgPackage,
    ThisBuild / version := "1.0.0",
    ThisBuild / idePackagePrefix := Some(basePackage),
    Global / excludeLintKeys += idePackagePrefix,
    ThisBuild / scalaVersion := "3.1.3",
    Global / cancelable := true,
    Global / fork := true,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Compile / compile / wartremoverErrors ++= warts,
    Test / compile / wartremoverErrors ++= warts,
    ThisBuild / semanticdbEnabled := true,
    ThisBuild / semanticdbVersion := scalafixSemanticdb.revision,
    Test / testOptions += Tests.Argument(MUnitFramework, "--exclude-tags=online"),
    Test / envVars := Map(
      "SBT_TEST_ENV_VARS" -> "true",
      "JDBC_DRIVER_CLASS_NAME" -> "org.postgresql.Driver",
      "JDBC_CONNECT_URL" -> "jdbc:postgresql://localhost:5432/test_db",
      "JDBC_USER" -> "test_jdbc_user",
      "JDBC_PASSWORD" -> "test_jdbc_password",
      "KAFKA_BOOTSTRAP_SERVERS" -> "localhost:29092",
      "KAFKA_CONSUMER_GROUP" -> "test_kafka_consumer_group",
      "KAFKA_TOPIC" -> "test_kafka_topic",
      "KAFKA_SCHEMA_REGISTRY" -> "http://localhost:8081",
    ),
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
      "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
    ),
  )

  implicit class ProjectSyntax(project: Project) {
    def root(rootName: String): Project =
      project.in(file(".")).settings(Seq(name := rootName, publish / skip := true, welcomeMessage))

    private[this] def module(path: String): Project =
      project.in(file(path)).settings(commonSettings(project.id))

    def application(path: String): Project =
      module(s"apps/$path")
        .settings(
          Seq(
            nativeImageOptions += "--force-fallback",
            nativeImageOptions += s"-H:ReflectionConfigurationFiles=${target.value / "native-image-configs" / "reflect-config.json"}",
            nativeImageOptions += s"-H:ConfigurationFileDirectories=${target.value / "native-image-configs"}",
            nativeImageOptions += "-H:+JNI",
            nativeImageVersion := "22.2.0",
            nativeImageJvm := "graalvm-java17",
            unusedCompileDependenciesFilter -= moduleFilter("org.scalameta", "svm-subs"),
          ),
        )
        .enablePlugins(NativeImagePlugin)

    def library(path: String): Project = module("libs/" ++ path)

    private[this] def dependencies_(dependencies: Seq[ModuleID]): Project =
      project.settings(libraryDependencies ++= dependencies)

    def mainDependencies(dependencies: ModuleID*): Project = dependencies_(dependencies)

    def optionalDependencies(dependencies: ModuleID*): Project =
      dependencies_(dependencies.map(_ % Optional))

    def providedDependencies(dependencies: ModuleID*): Project =
      dependencies_(dependencies.map(_ % Provided))

    def runtimeDependencies(dependencies: ModuleID*): Project =
      dependencies_(dependencies.map(_ % Runtime))

    def testDependencies(dependencies: ModuleID*): Project =
      dependencies_(dependencies.map(_ % Test))

    def unusedCompileDependencies(dependencies: ModuleID*): Project =
      dependencies_(dependencies).settings(
        dependencies.map(x =>
          unusedCompileDependenciesFilter -= moduleFilter(x.organization, x.name),
        ),
      )
  }
}
