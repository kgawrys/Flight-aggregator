package flightsaggregator.opensky.domain

import flightsaggregator.core.http.Error

case class OpenSkyStatesRequest()

case class FlightState(
  icao24: String,
  originCountry: String,
  timePosition: Option[BigDecimal],
  onGround: Boolean
)

case class OpenSkyHost(host: String) extends AnyVal

case class OpenSkyConfig(openSkyHost: OpenSkyHost)

sealed trait OpenSkyStatesResponse
object OpenSkyStatesResponse {
  case class States(time: Int, states: List[FlightState]) extends OpenSkyStatesResponse
  case class OpenSkyError(error: Error) extends OpenSkyStatesResponse
}