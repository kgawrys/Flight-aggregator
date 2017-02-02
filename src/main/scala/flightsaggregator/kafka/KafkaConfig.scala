package flightsaggregator.kafka

case class KafkaHost(host: String) extends AnyVal
case class KafkaPort(port: Int) extends AnyVal

case class KafkaConfig(kafkaHost: KafkaHost, kafkaPort: KafkaPort)
