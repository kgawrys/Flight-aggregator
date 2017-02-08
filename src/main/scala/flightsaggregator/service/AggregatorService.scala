package flightsaggregator.service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.{Materializer, ThrottleMode}
import akka.stream.scaladsl.{Flow, Sink}
import akka.{Done, NotUsed}
import flightsaggregator.core.AppConfig
import flightsaggregator.core.http.json.FlightAggregatorJsonFormats._
import flightsaggregator.kafka.KafkaConsumer.ConsumerMessage
import flightsaggregator.kafka.{KafkaConfig, KafkaConsumer}
import flightsaggregator.opensky.domain.FlightState
import flightsaggregator.service.AggregatorService.{OriginCountry, OriginFlights}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

object AggregatorService {
  type OriginCountry = String
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

  /*
    This solution is better the groupedWithin in terms of storing - as it only stores the aggregated result.
    On the other hand it needs to "warm up" - so two first cycles are useless.
   */
  private val stats = Flow[FlightState]
    .conflateWithSeed(f => Map(f.originCountry -> List(f)))(aggregateElements)
    // generates time based backpressure on conflate
    .throttle(1, appConfig.windowInterval second, 1, ThrottleMode.shaping)
    .map(_.mapValues(_.size))
    .map(_.toList.map(f => OriginFlights(f._1, f._2)))

  private def aggregateElements(flights: Map[OriginCountry, List[FlightState]], f: FlightState): Map[OriginCountry, List[FlightState]] =
    flights.get(f.originCountry) match {
      case Some(value) =>
        // filter duplicate ICAO24s
        if (!value.exists(_.icao24 == f.icao24)) flights + (f.originCountry -> (f :: value))
        else flights
      case None => flights + (f.originCountry -> List(f))
    }

  val aggregatingStream = kafkaSource
    .via(resumeFlowOnError(transformFlow)(logger))
    .via(resumeFlowOnError(stats)(logger))
    .to(loggingSink)

}
