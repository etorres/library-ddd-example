package es.eriktorr.library
package validated

import refined.types.NonEmptyString.NonEmptyStringValidationError.StringIsEmpty
import refined.types.UUID.UUIDValidationError.UUIDInvalidFormat
import refined.types.infrastructure.RefinedTypesGenerators.{nonEmptyStringGen, uuidGen}
import refined.types.{NonEmptyString, UUID}
import validated.ValidatedIO.validatedNecIO
import validated.ValidatedIOSuite.{testCaseGen, TestCase, TestResult}

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
          io.interceptMessage[ValidationErrors](ValidationErrors(errors).getMessage).map(_ => ())
    }
  }

object ValidatedIOSuite:
  final private case class TestResult(uuid: UUID, nonEmptyString: NonEmptyString)

  private object TestResult:
    def from(uuid: String, nonEmptyString: String): AllErrorsOr[TestResult] =
      (UUID.from(uuid), NonEmptyString.from(nonEmptyString)).mapN(TestResult.apply)

  final private case class TestCase(
      uuid: String,
      nonEmptyString: String,
      expectedValidation: AllErrorsOr[TestResult],
  )

  private[this] val invalidUUIDGen: Gen[String] =
    Gen.nonEmptyListOf(Gen.asciiPrintableChar).map(_.mkString(""))

  private[this] val emptyStringGen: Gen[String] = Gen.const("")

  private[this] val validTestCaseGen: Gen[TestCase] = for
    uuid <- uuidGen
    nonEmptyString <- nonEmptyStringGen
  yield TestCase(uuid.value, nonEmptyString.value, TestResult(uuid, nonEmptyString).validNec)

  private[this] val invalidUuidTestCaseGen: Gen[TestCase] = for
    uuid <- invalidUUIDGen
    nonEmptyString <- nonEmptyStringGen.map(_.value)
  yield TestCase(uuid, nonEmptyString, UUIDInvalidFormat.invalidNec)

  private[this] val invalidNonEmptyStringTestCaseGen: Gen[TestCase] = for
    uuid <- uuidGen.map(_.value)
    nonEmptyString <- emptyStringGen
  yield TestCase(uuid, nonEmptyString, StringIsEmpty.invalidNec)

  private[this] val allErrorsGen: Gen[TestCase] = for
    uuid <- invalidUUIDGen
    nonEmptyString <- emptyStringGen
  yield TestCase(
    uuid,
    nonEmptyString,
    Validated.Invalid(NonEmptyChain(UUIDInvalidFormat, StringIsEmpty)),
  )

  private val testCaseGen: Gen[TestCase] =
    Gen.frequency(
      1 -> validTestCaseGen,
      1 -> invalidUuidTestCaseGen,
      1 -> invalidNonEmptyStringTestCaseGen,
      1 -> allErrorsGen,
    )
