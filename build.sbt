import Dependencies._
import Settings.{fqClassNameFrom, sbtSettings, ProjectSyntax}

sbtSettings

lazy val `library-ddd-example` =
  project
    .root("library-ddd-example")
    .aggregate(commons, catalogue, lending)

lazy val catalogue = project
  .application("catalogue")
  .dependsOn(commons % "test->test;compile->compile")
  .mainDependencies(
    catsCore,
    catsEffect,
    catsEffectKernel,
    catsEffectStd,
    fs2Core,
    log4catsCore,
    log4catsSlf4j,
  )
  .runtimeDependencies(log4jApi, log4jCore, log4jSlf4jImpl)
  .testDependencies(
    munit,
    munitCatsEffect,
    munitScalacheck,
    scalacheckEffect,
    scalacheckEffectMunit,
  )
  .settings(Compile / mainClass := fqClassNameFrom("CatalogueApplication"))

lazy val commons =
  project.library("commons").mainDependencies(catsCore).testDependencies(munit, scalacheck)

lazy val lending =
  project
    .application("lending")
    .dependsOn(commons % "test->test;compile->compile")
    .mainDependencies(
      catsCore,
      catsEffect,
      catsEffectKernel,
      fs2Core,
      log4catsCore,
      log4catsSlf4j,
    )
    .runtimeDependencies(log4jApi, log4jCore, log4jSlf4jImpl)
    .testDependencies(
      munit,
      munitCatsEffect,
      munitScalacheck,
      scalacheckEffect,
      scalacheckEffectMunit,
    )
    .settings(Compile / mainClass := fqClassNameFrom("LendingApplication"))
