package es.eriktorr.library
package lending.model

import lending.model.Patron.PatronHoldsAndOverdueCheckouts

import cats.effect.IO

trait Patrons:
  def findBy(patronId: PatronId): IO[Option[PatronHoldsAndOverdueCheckouts]]

  def save(bookStateChanged: BookStateChanged): IO[Unit]
