package flightsaggregator.core.http.json

import flightsaggregator.opensky.domain.FlightState

trait FlightAggregatorJsonFormats extends FlightAggregatorJsonProtocol {
  implicit val flightStateFormat = jsonFormat4(FlightState.apply)
}

object FlightAggregatorJsonFormats extends FlightAggregatorJsonFormats