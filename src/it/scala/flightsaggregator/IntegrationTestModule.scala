package flightsaggregator

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import flightsaggregator.core.Setup
import org.scalatest.Suite

trait IntegrationTestModule extends Setup with ScalatestRouteTest  {
  self: Suite =>
    override lazy val logger: LoggingAdapter = Logging(system, getClass)
}
