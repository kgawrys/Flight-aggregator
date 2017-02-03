package flightsaggregator.aggregator

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{Materializer, ThrottleMode}
import akka.{Done, NotUsed}
import flightsaggregator.aggregator.AggregatorService.ConsumerMessage
import flightsaggregator.kafka.{KafkaConfig, KafkaConsumer}

import scala.concurrent.Future
import scala.concurrent.duration._

object AggregatorService {
  type ConsumerMessage = CommittableMessage[Array[Byte], String]
}

class AggregatorService(kafkaConsumer: KafkaConsumer, kafkaConfig: KafkaConfig, logger: LoggingAdapter)(implicit as: ActorSystem, mat: Materializer) {

  import flightsaggregator.stream.StreamHelpers._

  private val kafkaSource = kafkaConsumer.create("statesConsumer", kafkaConfig.stateTopic)(as)

  private val loggingFlow: Flow[ConsumerMessage, ConsumerMessage, NotUsed] =
    Flow[ConsumerMessage].map(m => { logger.info(m.toString); m })

  private val loggingSink: Sink[ConsumerMessage, Future[Done]] = Sink.foreach(elem => logger.info(s"Processing element: $elem"))

  val graph = kafkaSource
    .throttle(1, 2.second, 1, ThrottleMode.shaping)
    .via(resumeFlowOnError(loggingFlow)(logger))
    .to(Sink.ignore)

}
