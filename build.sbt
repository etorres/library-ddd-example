import Dependencies._
import Settings.{fqClassNameFrom, sbtSettings, ProjectSyntax}

sbtSettings

lazy val `library-ddd-example` =
  project
    .root("library-ddd-example")
    .aggregate(`book-instance`, catalogue, `commons-jdbc`, `commons-kafka`, `commons-lang`, lending)

lazy val catalogue = project
  .application("catalogue")
  .dependsOn(
    `book-instance` % "test->test;compile->compile",
    `commons-jdbc` % "test->test;compile->compile",
    `commons-kafka` % "test->test;compile->compile",
    `commons-lang` % "test->test;compile->compile",
  )
  .mainDependencies(
    catsCore,
    catsEffect,
    catsEffectKernel,
    catsEffectStd,
    catsFree,
    catsKernel,
    ciris,
    doobieCore,
    doobieFree,
    doobieHikari,
    fs2Kafka,
    hikariCP,
    log4catsCore,
    log4catsSlf4j,
    typename,
    vulcan,
  )
  .unusedCompileDependencies(doobiePostgres, log4jApi, log4jCore, log4jSlf4jImpl)
  .testDependencies(
    munit,
    munitCatsEffect,
    munitScalacheck,
    scalacheckEffect,
    scalacheckEffectMunit,
  )
  .settings(Compile / mainClass := fqClassNameFrom("catalogue.CatalogueApplication"))

lazy val lending =
  project
    .application("lending")
    .dependsOn(
      `book-instance` % "test->test;compile->compile",
      `commons-jdbc` % "test->test;compile->compile",
      `commons-kafka` % "test->test;compile->compile",
      `commons-lang` % "test->test;compile->compile",
    )
    .mainDependencies(
      catsCore,
      catsEffect,
      catsEffectKernel,
      catsEffectStd,
      catsFree,
      ciris,
      doobieCore,
      doobieFree,
      doobieHikari,
      fs2Core,
      fs2Kafka,
      hikariCP,
      log4catsCore,
      log4catsSlf4j,
      typename,
      scopt,
      vulcan,
    )
    .unusedCompileDependencies(doobiePostgres, log4jApi, log4jCore, log4jSlf4jImpl)
    .testDependencies(
      fs2kafkaVulcanTestkitMunit,
      munit,
      munitCatsEffect,
      munitScalacheck,
      scalacheckEffect,
      scalacheckEffectMunit,
    )
    .settings(Compile / mainClass := fqClassNameFrom("lending.LendingApplication"))

lazy val `book-instance` =
  project
    .library("book-instance")
    .dependsOn(`commons-lang` % "test->test;compile->compile")
    .mainDependencies(catsCore)
    .optionalDependencies(avro, catsFree, doobieCore, doobiePostgres, vulcan)
    .testDependencies(log4jApi, log4jCore, log4jSlf4jImpl, munit, scalacheck)

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
      doobieHikari,
      hikariCP,
    )
    .testDependencies(
      munit,
      munitCatsEffect,
      munitScalacheck,
      scalacheckEffect,
      scalacheckEffectMunit,
    )

lazy val `commons-kafka` = project
  .library("commons-kafka")
  .dependsOn(`commons-lang` % "test->test;compile->compile")
  .mainDependencies(
    avro,
    catsCore,
    catsEffect,
    catsEffectKernel,
    ciris,
    fs2Core,
    fs2Kafka,
    fs2KafkaVulcan,
    schemaRegistryClient,
    vulcan,
  )
  .testDependencies(
    log4catsCore,
    munit,
    munitCatsEffect,
    munitScalacheck,
    scalacheckEffect,
    scalacheckEffectMunit,
  )

lazy val `commons-lang` =
  project
    .library("commons-lang")
    .mainDependencies(catsCore, catsEffect, catsEffectKernel)
    .optionalDependencies(avro, vulcan)
    .testDependencies(
      munit,
      munitCatsEffect,
      munitScalacheck,
      scalacheckEffect,
      scalacheckEffectMunit,
    )
