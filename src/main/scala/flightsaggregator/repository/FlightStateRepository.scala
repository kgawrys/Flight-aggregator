package flightsaggregator.repository

import flightsaggregator.core.cassandra.AppDatabase
import flightsaggregator.opensky.domain.FlightState

import scala.concurrent.Future

class FlightStateRepository(db: AppDatabase) {
  def storeFlightEvent(flightState: FlightState): Future[Boolean] = {
    db.flightstates.store(flightState)
  }
}
