package es.eriktorr.library
package lending

import shared.ValidationErrors
import shared.refined.types.UUID

import cats.data.Validated

final case class LendingParameters(libraryBranchId: UUID):
  def asString: String = s"library-branch-id=$libraryBranchId"

object LendingParameters:
  import scopt.OParser

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  implicit private val uuidRead: scopt.Read[UUID] =
    scopt.Read.reads(x =>
      UUID.from(x) match
        case Validated.Valid(value) => value
        case Validated.Invalid(errors) =>
          throw IllegalArgumentException(s"'$x' is not a UUID.", ValidationErrors(errors)),
    )

  private[this] val builder = OParser.builder[LendingParameters]
  private[this] val argParser =
    import builder.*
    OParser.sequence(
      programName("book-lending-library"),
      head("book-lending-library", "1.x"),
      opt[UUID]('b', "branch")
        .required()
        .valueName("<uuid>")
        .action((x, c) => c.copy(libraryBranchId = x))
        .text("branch is a required UUID property")
        .validate(x =>
          if x != UUID.nil then success else failure("Option --branch must be not nil"),
        ),
      help("help").text("prints this usage text"),
    )

  def parametersFrom(args: List[String]): Option[LendingParameters] =
    OParser.parse(argParser, args, LendingParameters(UUID.nil))

  def usage: String = OParser.usage(argParser)
