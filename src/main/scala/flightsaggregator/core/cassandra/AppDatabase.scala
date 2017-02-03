package flightsaggregator.core.cassandra

import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.dsl._

class AppDatabase(val keyspace: KeySpaceDef) extends Database(keyspace) {
}