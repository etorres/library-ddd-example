package es.eriktorr.library
package lending.model

import book.model.BookId

import java.time.Instant

final case class Patron(patronId: PatronId, patronType: PatronType)

object Patron:
  final case class Hold(
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
      till: Instant,
  )

  final case class OverdueCheckout(bookId: BookId, libraryBranchId: LibraryBranchId)

  final case class PatronHoldsAndOverdueCheckouts(
      patron: Patron,
      holds: List[Hold],
      overdueCheckouts: List[OverdueCheckout],
  ):
    def patronId: PatronId = patron.patronId
