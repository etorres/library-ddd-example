package es.eriktorr.library
package shared.validated

import shared.UUIDValidationError.UUIDInvalidFormat
import shared.refined.types.NonEmptyString
import shared.refined.types.NonEmptyString.NonEmptyStringValidationError.StringIsEmpty
import shared.refined.types.infrastructure.RefinedTypesGenerators.{eventIdGen, nonEmptyStringGen}
import shared.validated.AllErrorsOr
import shared.validated.ValidatedIO.validatedNecIO
import shared.validated.ValidatedIOSuite.{testCaseGen, TestCase, TestResult}
import shared.{EventId, ValidationErrors}

import cats.data.{NonEmptyChain, Validated}
import cats.syntax.all.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class ValidatedIOSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should lift a validated value into the IO context raising the throwable when invalid") {
    forAllF(testCaseGen) { case TestCase(uuid, nonEmptyString, expectedValidation) =>
      val io = TestResult.from(uuid, nonEmptyString).validated
      expectedValidation match
        case Validated.Valid(value) => io.assertEquals(value)
        case Validated.Invalid(errors) =>
          io.interceptMessage[ValidationErrors](shared.ValidationErrors(errors).getMessage)
            .map(_ => ())
    }
  }

object ValidatedIOSuite:
  final private case class TestResult(eventId: EventId, nonEmptyString: NonEmptyString)

  private object TestResult:
    def from(eventId: String, nonEmptyString: String): AllErrorsOr[TestResult] =
      (EventId.from(eventId), NonEmptyString.from(nonEmptyString)).mapN(TestResult.apply)

  final private case class TestCase(
      uuid: String,
      nonEmptyString: String,
      expectedValidation: AllErrorsOr[TestResult],
  )

  private[this] val invalidUUIDGen: Gen[String] =
    Gen.nonEmptyListOf(Gen.asciiPrintableChar).map(_.mkString(""))

  private[this] val emptyStringGen: Gen[String] = Gen.const("")

  private[this] val validTestCaseGen: Gen[TestCase] = for
    eventId <- eventIdGen
    nonEmptyString <- nonEmptyStringGen
  yield TestCase(
    eventId.asString,
    nonEmptyString.value,
    TestResult(eventId, nonEmptyString).validNec,
  )

  private[this] val invalidUuidTestCaseGen: Gen[TestCase] = for
    uuid <- invalidUUIDGen
    nonEmptyString <- nonEmptyStringGen.map(_.value)
  yield TestCase(
    uuid,
    nonEmptyString,
    UUIDInvalidFormat(IllegalArgumentException("invalid UUID")).invalidNec,
  )

  private[this] val invalidNonEmptyStringTestCaseGen: Gen[TestCase] = for
    eventId <- eventIdGen.map(_.asString)
    nonEmptyString <- emptyStringGen
  yield TestCase(eventId, nonEmptyString, StringIsEmpty.invalidNec)

  private[this] val allErrorsGen: Gen[TestCase] = for
    uuid <- invalidUUIDGen
    nonEmptyString <- emptyStringGen
  yield TestCase(
    uuid,
    nonEmptyString,
    Validated.Invalid(
      NonEmptyChain(UUIDInvalidFormat(IllegalArgumentException("invalid UUID")), StringIsEmpty),
    ),
  )

  private val testCaseGen: Gen[TestCase] =
    Gen.frequency(
      1 -> validTestCaseGen,
      1 -> invalidUuidTestCaseGen,
      1 -> invalidNonEmptyStringTestCaseGen,
      1 -> allErrorsGen,
    )
