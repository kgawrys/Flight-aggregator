package flightsaggregator.core.http

import java.util.UUID

case class Error private (message: String, logUuid: Option[UUID])

object Error {
  def apply(message: String): Error = Error(message, None)
  def apply(message: String, logUuid: UUID): Error = Error(message, Option(logUuid))
}
