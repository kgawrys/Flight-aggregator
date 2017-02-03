package flightsaggregator.kafka

case class KafkaHostname(name: String) extends AnyVal
case class KafkaPort(port: Int) extends AnyVal
case class KafkaTopic(topic: String) extends AnyVal

case class KafkaConfig(kafkaHostname: KafkaHostname, kafkaPort: KafkaPort, stateTopic: KafkaTopic) {
  def kafkaHost = s"${kafkaHostname.name}:${kafkaPort.port}"
}
