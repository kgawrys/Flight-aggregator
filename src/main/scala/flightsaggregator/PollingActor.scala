package flightsaggregator

import akka.actor.Actor
import akka.event.LoggingAdapter
import akka.stream.Materializer
import flightsaggregator.PollingActor.{Poll, Stop}
import flightsaggregator.opensky.OpenSkyService
import flightsaggregator.opensky.domain.OpenSkyStatesRequest

import scala.concurrent.ExecutionContext

class PollingActor(logger: LoggingAdapter, openSkyService: OpenSkyService)(implicit mat: Materializer, ex: ExecutionContext) extends Actor {
  def receive = {
    case Poll =>
      logger.info("Received poll request")
      val states = openSkyService.getStates(new OpenSkyStatesRequest())
      states.map(resp => logger.info(s"response have: ${resp.right.get.states.size}"))
    case Stop =>
      logger.info("Shutting down polling actor")
      context.stop(self)
    case _ =>
      logger.warning("Unknown message received")
  }
}

object PollingActor {
  case object Poll
  case object Stop
}