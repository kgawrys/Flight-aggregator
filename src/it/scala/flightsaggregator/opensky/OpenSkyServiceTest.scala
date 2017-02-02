package flightsaggregator.opensky

import flightsaggregator.opensky.domain.{OpenSkyStatesRequest, OpenSkyStatesResponse}
import flightsaggregator.{IntegrationTestModule, IntegrationTestSuite}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Minute, Span}

class OpenSkyServiceTest extends IntegrationTestSuite with IntegrationTestModule {

  behavior of "OpenSky service"

  it should "return list states" in {
    val req = OpenSkyStatesRequest()
    val timeout = Timeout(Span(1, Minute))
    whenReady(openSkyService.getStates(req), timeout) {
      case Right(statesResponse) =>
        statesResponse shouldBe a[OpenSkyStatesResponse]
        logger.info(statesResponse.toString)
      case _ => fail("Fail to call OpenSky states endpoint")
    }
  }
}
