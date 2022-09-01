package es.eriktorr.library
package shared

opaque type Version = Long

object Version:
  def from(value: Long): Version = value

  extension (version: Version)
    def value: Long = version

    def inc: Version = from(version + 1L)

  val init: Version = from(0L)
