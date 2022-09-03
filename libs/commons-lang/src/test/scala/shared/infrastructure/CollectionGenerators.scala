package es.eriktorr.library
package shared.infrastructure

import org.scalacheck.Gen

import scala.annotation.tailrec

object CollectionGenerators:
  def nDistinct[T](number: Int, elementGen: Gen[T]): Gen[List[T]] =
    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def generate(accumulator: List[T]): Gen[List[T]] =
      if accumulator.size == number then Gen.const(accumulator)
      else
        for
          candidate <- elementGen
          result <- generate(
            if accumulator.contains(candidate) then accumulator else candidate :: accumulator,
          )
        yield result

    generate(List.empty)
