package flightsaggregator

import akka.event.{Logging, LoggingAdapter}
import flightsaggregator.core.Setup
import org.scalatest.Suite

trait IntegrationTestModule extends Setup {
  self: Suite =>
    override lazy val logger: LoggingAdapter = Logging(system, getClass)
}
