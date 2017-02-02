package flightsaggregator.opensky.domain

case class OpenSkyStatesRequest()

case class OpenSkyStatesResponse()

case class OpenSkyHost(host: String) extends AnyVal

case class OpenSkyConfig(host: OpenSkyHost)