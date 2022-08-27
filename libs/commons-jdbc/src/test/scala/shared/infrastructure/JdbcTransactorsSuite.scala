package es.eriktorr.library
package shared.infrastructure

import shared.refined.types.NonEmptyString

import cats.effect.IO
import doobie.Transactor
import doobie.util.ExecutionContexts
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Test

abstract class JdbcTransactorsSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  def jdbcTestConfig: JdbcTestConfig

  private[this] val connectEc = ExecutionContexts.synchronous

  val transactorFixture: Fixture[Transactor[IO]] =
    ResourceSuiteLocalFixture(
      "doobie-transactor",
      JdbcTestTransactor.testTransactorResource(
        jdbcTestConfig,
        connectEc,
      ),
    )

  override def munitFixtures: Seq[Fixture[?]] = List(transactorFixture)
