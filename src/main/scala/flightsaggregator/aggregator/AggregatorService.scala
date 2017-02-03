package flightsaggregator.aggregator

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{Materializer, ThrottleMode}
import akka.{Done, NotUsed}
import flightsaggregator.aggregator.AggregatorService.{ConsumerMessage, OriginFlights}
import flightsaggregator.core.AppConfig
import flightsaggregator.kafka.{KafkaConfig, KafkaConsumer}
import flightsaggregator.opensky.domain.FlightState
import spray.json._
import flightsaggregator.core.http.json.FlightAggregatorJsonFormats._

import scala.concurrent.Future
import scala.concurrent.duration._

object AggregatorService {
  type ConsumerMessage = CommittableMessage[Array[Byte], String]
  case class OriginFlights(origin: String, count: Int)
}

class AggregatorService(kafkaConsumer: KafkaConsumer, kafkaConfig: KafkaConfig, logger: LoggingAdapter, appConfig: AppConfig)(implicit as: ActorSystem, mat: Materializer) {

  import flightsaggregator.stream.StreamHelpers._

  private val kafkaSource = kafkaConsumer.create("statesConsumer", kafkaConfig.stateTopic)(as)

  private val loggingFlow: Flow[ConsumerMessage, ConsumerMessage, NotUsed] =
    Flow[ConsumerMessage].map(m => { logger.info(m.toString); m })

  private val loggingSink: Sink[Iterable[OriginFlights], Future[Done]] = Sink.foreach(elem => elem.foreach {
    fo => logger.info(statusMessage(fo))
  })

  private def statusMessage(ofc: OriginFlights) =
    s"${ofc.origin}, ${ofc.count} flights originated from the ${ofc.origin} in the last ${appConfig.windowInterval} seconds"

  val transformFlow: Flow[ConsumerMessage, FlightState, NotUsed] =
    Flow[ConsumerMessage]
      .map(m => m.record.value.parseJson.convertTo[FlightState])

  val stats = Flow[FlightState]
    .groupedWithin(10000, appConfig.windowInterval second)
    .map { s => s.groupBy(_.originCountry).map { kv => OriginFlights(kv._1, kv._2.size) } }

  val graph = kafkaSource
    //    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .via(resumeFlowOnError(transformFlow)(logger))
    .via(resumeFlowOnError(stats)(logger))
    .to(loggingSink)

}
