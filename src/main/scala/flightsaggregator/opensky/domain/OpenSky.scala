package flightsaggregator.opensky.domain

case class OpenSkyStatesRequest()

case class FlightState(
  icao24: String,
  originCountry: String,
  timePosition: Option[BigDecimal],
  onGround: Boolean
)

case class OpenSkyStatesResponse(time: Int, states: List[FlightState])

case class OpenSkyHost(host: String) extends AnyVal

case class OpenSkyConfig(openSkyHost: OpenSkyHost)