package es.eriktorr.library
package lending

import lending.model.LibraryBranchId
import shared.ValidationErrors

import cats.data.Validated

final case class LendingParameters(libraryBranchId: LibraryBranchId):
  def asString: String = s"library-branch-id=$libraryBranchId"

object LendingParameters:
  import scopt.OParser

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  implicit private val libraryBranchIdRead: scopt.Read[LibraryBranchId] =
    scopt.Read.reads(x =>
      LibraryBranchId.from(x) match
        case Validated.Valid(value) => value
        case Validated.Invalid(errors) =>
          throw IllegalArgumentException(s"'$x' is not a UUID.", ValidationErrors(errors)),
    )

  private[this] val builder = OParser.builder[LendingParameters]
  private[this] val argParser =
    import builder.*
    OParser.sequence(
      programName("lending"),
      head("lending", "1.x"),
      opt[LibraryBranchId]('b', "branch")
        .required()
        .valueName("<uuid>")
        .action((x, c) => c.copy(libraryBranchId = x))
        .text("branch is a required UUID property")
        .validate(x =>
          if x != LibraryBranchId.nil then success else failure("Option --branch must be not nil"),
        ),
      help("help").text("prints this usage text"),
    )

  def from(args: List[String]): Option[LendingParameters] =
    OParser.parse(argParser, args, LendingParameters(LibraryBranchId.nil))

  def usage: String = OParser.usage(argParser)
