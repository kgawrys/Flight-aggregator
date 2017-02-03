package flightsaggregator.kafka

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object KafkaConsumer {
  type ConsumerMessage = CommittableMessage[Array[Byte], String]
}

class KafkaConsumer(kafkaConfig: KafkaConfig) {

  def create(groupId: String, topic: KafkaTopic)(implicit system: ActorSystem): Source[ConsumerMessage.CommittableMessage[Array[Byte], String], Consumer.Control] = {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaConfig.kafkaHost)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic.topic))
  }
}
