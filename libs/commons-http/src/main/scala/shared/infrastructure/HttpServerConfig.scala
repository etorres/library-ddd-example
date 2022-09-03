package es.eriktorr.library
package shared.infrastructure

import com.comcast.ip4s.{Host, Port}

final case class HttpServerConfig(host: Host, port: Port):
  def asString: String = s"http-host=$host, http-port=$port"
