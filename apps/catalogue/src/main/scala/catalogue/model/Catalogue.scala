package es.eriktorr.library
package catalogue.model

import book.model.{Book, BookInstance}

import cats.effect.IO

trait Catalogue:
  def add(book: Book): IO[Unit]
  def add(bookInstance: BookInstance): IO[Unit]
