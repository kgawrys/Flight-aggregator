package flightsaggregator.service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.{Done, NotUsed}
import flightsaggregator.core.AppConfig
import flightsaggregator.kafka.{KafkaConfig, KafkaConsumer}
import flightsaggregator.opensky.domain.FlightState
import flightsaggregator.service.AggregatorService.OriginFlights
import spray.json._
import flightsaggregator.core.http.json.FlightAggregatorJsonFormats._
import flightsaggregator.kafka.KafkaConsumer.ConsumerMessage

import scala.concurrent.Future
import scala.concurrent.duration._

object AggregatorService {
  case class OriginFlights(origin: String, count: Int)
}

class AggregatorService(kafkaConsumer: KafkaConsumer, kafkaConfig: KafkaConfig, logger: LoggingAdapter, appConfig: AppConfig)(implicit as: ActorSystem, mat: Materializer) {

  import flightsaggregator.stream.StreamHelpers._

  private val kafkaSource = kafkaConsumer.create("aggregatorConsumer", kafkaConfig.stateTopic)(as)

  private val loggingFlow: Flow[ConsumerMessage, ConsumerMessage, NotUsed] =
    Flow[ConsumerMessage].map(m => { logger.info(m.toString); m })

  private val loggingSink: Sink[Iterable[OriginFlights], Future[Done]] =
    Sink.foreach(fomap =>
      logger.info(fomap.map { fo => summaryMessage(fo) }.mkString("\n")))

  private def summaryMessage(ofc: OriginFlights) =
    s"${ofc.origin}, ${ofc.count} flights originated from the ${ofc.origin} in the last ${appConfig.windowInterval} seconds"

  private val transformFlow: Flow[ConsumerMessage, FlightState, NotUsed] =
    Flow[ConsumerMessage]
      .map(m => m.record.value.parseJson.convertTo[FlightState])

  private val stats = Flow[FlightState]
    .groupedWithin(10000, appConfig.windowInterval second)
    .map { s => s.groupBy(_.originCountry).map { kv => OriginFlights(kv._1, kv._2.size) } }

  val graph = kafkaSource
    .via(resumeFlowOnError(transformFlow)(logger))
    .via(resumeFlowOnError(stats)(logger))
    .to(loggingSink)

}
