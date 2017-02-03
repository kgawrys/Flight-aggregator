package flightsaggregator.service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import flightsaggregator.core.http.json.FlightAggregatorJsonFormats._
import flightsaggregator.kafka.KafkaConsumer.ConsumerMessage
import flightsaggregator.kafka.{KafkaConfig, KafkaConsumer}
import flightsaggregator.opensky.domain.FlightState
import flightsaggregator.repository.FlightStateRepository
import flightsaggregator.stream.StreamHelpers._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class FlightStateService(kafkaConsumer: KafkaConsumer, kafkaConfig: KafkaConfig, flightStatesRepository: FlightStateRepository, logger: LoggingAdapter)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer) {

  private val kafkaSource = kafkaConsumer.create("savingConsumer", kafkaConfig.stateTopic)(as)

  private val transformFlow: Flow[ConsumerMessage, FlightState, NotUsed] =
    Flow[ConsumerMessage]
      .map(m => m.record.value.parseJson.convertTo[FlightState])

  private val cassandraWriterFlow: Flow[FlightState, Unit, NotUsed] =
    Flow[FlightState]
      .mapAsyncUnordered(parallelism = 4) { flight => saveFlightState(flight) }

  val savingStream = kafkaSource
    .via(resumeFlowOnError(transformFlow)(logger))
    .via(resumeFlowOnError(cassandraWriterFlow)(logger))
    .to(Sink.ignore)

  def saveFlightState(flightState: FlightState): Future[Unit] = {
    flightStatesRepository.storeFlightEvent(flightState).map {
      case true  => Unit
      case false => logger.error(s"Failed to save element: ${flightState.toString}")
    }
  }
}
