package flightsaggregator.opensky.domain

case class OpenSkyStatesRequest()

case class OpenSkyStatesResponse(time: Int, states: List[String])

case class OpenSkyHost(host: String) extends AnyVal

case class OpenSkyConfig(openSkyHost: OpenSkyHost)