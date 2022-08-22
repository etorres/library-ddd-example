import Dependencies._
import Settings.{fqClassNameFrom, sbtSettings, ProjectSyntax}

sbtSettings

lazy val `library-ddd-example` =
  project
    .root("library-ddd-example")
    .aggregate(`commons-jdbc`, `commons-lang`, catalogue, lending)

lazy val catalogue = project
  .application("catalogue")
  .dependsOn(
    `book-model` % "test->test;compile->compile",
    `commons-lang` % "test->test;compile->compile",
    `commons-jdbc` % "test->test;compile->compile",
  )
  .mainDependencies(
    catsCore,
    catsEffect,
    catsEffectKernel,
    catsEffectStd,
    catsFree,
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
      `commons-lang` % "test->test;compile->compile",
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
    .dependsOn(`commons-lang` % "test->test;compile->compile")
    .mainDependencies(catsCore)
    .testDependencies(munit, scalacheck)

lazy val `commons-jdbc` =
  project
    .library("commons-jdbc")
    .dependsOn(`commons-lang` % "test->test;compile->compile")
    .mainDependencies(
      catsCore,
      catsEffect,
      catsEffectKernel,
      ciris,
      doobieCore,
      doobieFree,
      doobieHikari,
      doobiePostgres,
      hikariCP,
    )
    .testDependencies(
      munit,
      munitCatsEffect,
      munitScalacheck,
      scalacheckEffect,
      scalacheckEffectMunit,
    )

lazy val `commons-lang` =
  project
    .library("commons-lang")
    .mainDependencies(catsCore, catsEffect)
    .testDependencies(munit, scalacheck)
