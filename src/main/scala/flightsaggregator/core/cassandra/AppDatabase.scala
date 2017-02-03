package flightsaggregator.core.cassandra

import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.dsl._
import flightsaggregator.repository.FlightStatesWithConnector

class AppDatabase(val keyspace: KeySpaceDef) extends Database(keyspace) {
  object flightstates extends FlightStatesWithConnector with keyspace.Connector
}