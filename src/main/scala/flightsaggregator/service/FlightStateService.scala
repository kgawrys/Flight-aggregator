package flightsaggregator.service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import flightsaggregator.opensky.domain.FlightState
import flightsaggregator.repository.FlightStateRepository

import scala.concurrent.ExecutionContext

class FlightStateService(flightStatesRepository: FlightStateRepository, logger: LoggingAdapter)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer) {

  def saveFlightState(flightState: FlightState) = {
    flightStatesRepository.storeFlightEvent(flightState).map {
      case true  =>
        logger.info("Element Saved successfully")
      case false =>
        logger.error(s"Failed to save element: ${flightState.toString}")
    }
  }
}
