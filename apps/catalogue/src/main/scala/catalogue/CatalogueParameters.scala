package es.eriktorr.library
package catalogue

final case class CatalogueParameters():
  def asString: String = ""

object CatalogueParameters:
  def from(args: List[String]): Option[CatalogueParameters] = Some(CatalogueParameters())

  def usage: String = ""
