package flightsaggregator

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait IntegrationTestSuite extends FlatSpecLike with ScalaFutures with Matchers with GivenWhenThen with BeforeAndAfterAll
