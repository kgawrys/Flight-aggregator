package flightsaggregator.repository

case class CassandraConfig(
  hostname: String,
  port: Int,
  replicationStrategy: String,
  replicationFactor: String,
  defaultConsistencyLevel: String,
  keyspace: String
)
