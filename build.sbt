import Dependencies._
import Settings.{fqClassNameFrom, sbtSettings, ProjectSyntax}

sbtSettings

lazy val `library-ddd-example` =
  project
    .root("library-ddd-example")
    .aggregate(commons, catalogue, lending)

lazy val catalogue = project
  .application("catalogue")
  .dependsOn(
    `book-model` % "test->test;compile->compile",
    commons % "test->test;compile->compile",
  )
  .mainDependencies(
    catsCore,
    catsEffect,
    catsEffectKernel,
    catsEffectStd,
    doobieCore,
    doobieFree,
    doobieHikari,
    doobiePostgres,
    fs2Core,
    hikariCP,
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

lazy val lending =
  project
    .application("lending")
    .dependsOn(
      `book-model` % "test->test;compile->compile",
      commons % "test->test;compile->compile",
    )
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

lazy val `book-model` =
  project
    .library("book-model")
    .dependsOn(commons % "test->test;compile->compile")
    .mainDependencies(catsCore)
    .testDependencies(munit, scalacheck)

lazy val commons =
  project.library("commons").mainDependencies(catsCore).testDependencies(munit, scalacheck)
