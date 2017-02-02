package flightsaggregator.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.{ExecutionContext, Future}

class KafkaProducer(kafkaConfig: KafkaConfig, logger: LoggingAdapter)(implicit as: ActorSystem, mat: Materializer, ec: ExecutionContext) {

  private val kafkaHost = s"${kafkaConfig.kafkaHost.host}:${kafkaConfig.kafkaPort.port}"

  val producerSettings = ProducerSettings(as, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(kafkaHost)
  logger.info(s"Initializing kafka writer on kafka host: $kafkaHost")

  val asSink: Sink[ProducerRecord[Array[Byte], String], Future[Done]] = Producer.plainSink(producerSettings)
}
