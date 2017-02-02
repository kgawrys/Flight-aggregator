package flightsaggregator.stream

import akka.event.LoggingAdapter
import akka.stream.ActorAttributes._
import akka.stream.Supervision.{Resume, Stop, _}
import akka.stream.scaladsl.{Flow, Sink}

object StreamHelpers {

  def resumingDecider(implicit logger: LoggingAdapter): Decider = e => {
    logger.error(e, "Resuming after error")
    Resume
  }

  def stoppingDecider(implicit logger: LoggingAdapter): Decider = e => {
    logger.error(e, "Stopping after error")
    Stop
  }

  def resumeFlowOnError[In, Out, Mat](flow: Flow[In, Out, Mat])(implicit logger: LoggingAdapter) =
    flow.withAttributes(supervisionStrategy(resumingDecider))

  def resumeSinkOnError[In, Out, Mat](sink: Sink[In, Out])(implicit logger: LoggingAdapter) =
    sink.withAttributes(supervisionStrategy(resumingDecider))

  def stopSinkOnError[In, Out, Mat](sink: Sink[In, Out])(implicit logger: LoggingAdapter) =
    sink.withAttributes(supervisionStrategy(stoppingDecider))

}
