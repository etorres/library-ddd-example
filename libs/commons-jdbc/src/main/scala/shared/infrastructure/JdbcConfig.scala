package es.eriktorr.library
package shared.infrastructure

import shared.refined.types.NonEmptyString

import ciris.Secret

final case class JdbcConfig(
    driverClassName: NonEmptyString,
    connectUrl: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
):
  def asString: String =
    import scala.language.unsafeNulls
    s"""driver-class-name=$driverClassName, 
       |connect-url=$connectUrl, 
       |user=$user, 
       |password=$password""".stripMargin.replaceAll("\\R", "")
