import sbt._

trait Cats {
  private[this] val organization = "org.typelevel"

  private[this] val catsVersion = "2.8.0"
  private[this] val catsEffectVersion = "3.3.14"
  private[this] val kittensVersion = "3.0.0-M4"

  val catsCore = organization %% "cats-core" % catsVersion
  val catsFree = organization %% "cats-free" % catsVersion
  val catsKernel = organization %% "cats-kernel" % catsVersion

  val catsEffect = organization %% "cats-effect" % catsEffectVersion
  val catsEffectKernel = organization %% "cats-effect-kernel" % catsEffectVersion
  val catsEffectStd = organization %% "cats-effect-std" % catsEffectVersion

  val kittens = organization %% "kittens" % kittensVersion
}

trait Ciris {
  private[this] val organization = "is.cir"
  private[this] val version = "2.3.3"

  val ciris = organization %% "ciris" % version
}

trait Doobie {
  private[this] val organization = "org.tpolecat"
  private[this] val version = "1.0.0-RC2"

  val doobieCore = organization %% "doobie-core" % version
  val doobieFree = organization %% "doobie-free" % version
  val doobieHikari = organization %% "doobie-hikari" % version
  val doobiePostgres = organization %% "doobie-postgres" % version
}

trait Fs2 {
  private[this] val organization = "co.fs2"
  private[this] val version = "3.2.12"

  val fs2Core = organization %% "fs2-core" % version
  val fs2Io = organization %% "fs2-io" % version
}

trait Hikari {
  private[this] val organization = "com.zaxxer"

  private[this] val version = "5.0.1"

  val hikariCP = (organization % "HikariCP" % version).exclude("org.slf4j", "slf4j-api")
}

trait Log4cats {
  private[this] val organization = "org.typelevel"
  private[this] val version = "2.4.0"

  val log4catsCore = organization %% "log4cats-core_sjs1" % version
  val log4catsSlf4j = organization %% "log4cats-slf4j" % version
}

trait Log4j {
  private[this] val organization = "org.apache.logging.log4j"
  private[this] val version = "2.18.0"

  val log4jApi = organization % "log4j-api" % version
  val log4jCore = organization % "log4j-core" % version
  val log4jSlf4jImpl = organization % "log4j-slf4j-impl" % version
}

trait Munit {
  private[this] val scalametaOrg = "org.scalameta"
  private[this] val scalametaVersion = "0.7.29"

  private[this] val typelevelOrg = "org.typelevel"
  private[this] val scalacheckEffectVersion = "1.0.4"
  private[this] val munitCatsEffect3Version = "1.0.7"

  val munit = scalametaOrg %% "munit" % scalametaVersion
  val munitScalacheck = scalametaOrg %% "munit-scalacheck" % scalametaVersion
  val munitCatsEffect = typelevelOrg %% "munit-cats-effect-3" % munitCatsEffect3Version
  val scalacheckEffect = typelevelOrg %% "scalacheck-effect" % scalacheckEffectVersion
  val scalacheckEffectMunit = typelevelOrg %% "scalacheck-effect-munit" % scalacheckEffectVersion
}

trait ScalaCheck {
  private[this] val organization = "org.scalacheck"
  private[this] val version = "1.16.0"

  val scalacheck = organization %% "scalacheck" % version
}

trait Scopt {
  private[this] val organization = "com.github.scopt"
  private[this] val version = "4.1.0"

  val scopt = organization %% "scopt" % version
}

object Dependencies
    extends Cats
    with Ciris
    with Doobie
    with Fs2
    with Hikari
    with Log4cats
    with Log4j
    with Munit
    with ScalaCheck
    with Scopt
