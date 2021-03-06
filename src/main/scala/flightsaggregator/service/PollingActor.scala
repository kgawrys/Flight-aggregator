package flightsaggregator.service

import akka.NotUsed
import akka.actor.Actor
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Source}
import flightsaggregator.core.http.json.FlightAggregatorJsonFormats._
import flightsaggregator.kafka.{KafkaConfig, KafkaProducer}
import flightsaggregator.opensky.OpenSkyService
import flightsaggregator.opensky.domain._
import flightsaggregator.service.PollingActor.{Poll, ProdMessage, Stop}
import flightsaggregator.stream.StreamHelpers
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._

import scala.concurrent.ExecutionContext

object PollingActor {
  type ProdMessage = ProducerRecord[Array[Byte], String]
  case object Poll
  case object Stop
}

class PollingActor(
    logger: LoggingAdapter,
    openSkyService: OpenSkyService,
    kafkaProducer: KafkaProducer,
    kafkaConfig: KafkaConfig
)(implicit mat: Materializer, ex: ExecutionContext) extends Actor {
  import StreamHelpers._

  def receive = {
    case Poll =>
      logger.info("Received poll request")
      val states = openSkyService.getStates(new OpenSkyStatesRequest())
      states.map {
        case resp: OpenSkyStatesResponse.States =>
          logger.info(s"Acquired ${resp.states.size} states from OpenSky response")
          Source.apply(resp.states.map(toFlightStateMsg))
            .via(resumeFlowOnError(toKafkaRecord)(logger))
            .toMat(kafkaProducer.asSink)(Keep.both)
            .run()
        case e: OpenSkyStatesResponse.OpenSkyError =>
          logger.error(s"Error during connection with OpenSky: ${e.error.message}")
      }
    case Stop =>
      logger.info("Shutting down polling actor")
      context.stop(self)
    case _ =>
      logger.warning("Unknown message received")
  }

  private def toFlightStateMsg(s: OpenSkyState) = FlightState(s.icao24, s.originCountry, s.timePosition, s.onGround)

  private val toKafkaRecord: Flow[FlightState, ProdMessage, NotUsed] =
    Flow[FlightState]
      .map(s => new ProducerRecord(
        kafkaConfig.stateTopic.topic,
        0,
        "".getBytes("utf8"),
        s.toJson.toString
      ))

}

