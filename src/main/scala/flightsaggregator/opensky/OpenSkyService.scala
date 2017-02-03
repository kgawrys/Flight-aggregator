package flightsaggregator.opensky

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, StatusCode, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import flightsaggregator.core.http.Error
import flightsaggregator.core.http.json.FlightAggregatorJsonProtocol._
import flightsaggregator.opensky.domain.OpenSkyStatesResponse.States
import flightsaggregator.opensky.domain._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object OpenSkyService {
  type OpenSkyResponse[T] = Either[Error, T]
  implicit val statesFormat = jsonFormat2(States.apply)
}

class OpenSkyService(config: OpenSkyConfig, logger: LoggingAdapter)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer) {

  import OpenSkyService._

  private val host = config.openSkyHost.host

  def getStates(request: OpenSkyStatesRequest): Future[OpenSkyStatesResponse] = {
    val statesRequest = buildStatesRequest(request)

    logger.debug("[OpenSkyService] request sent to OpenSky {} ", request)
    Http().singleRequest(statesRequest).flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { entity =>
        //        logRequestResponse(statesRequest, response.status, entity)
        response.status match {
          case OK => Unmarshal(entity).to[OpenSkyStatesResponse.States]
          case _  => Future.successful(OpenSkyStatesResponse.OpenSkyError(Error("Couldn't retrieve OpenSky states.")))
        }
      }
    }
  }

  private def buildStatesRequest(request: OpenSkyStatesRequest) = RequestBuilding.Get(Uri(s"$host/states/all"))

  private def logRequestResponse(request: HttpRequest, status: StatusCode, response: HttpEntity.Strict): Unit =
    logger.info(infoMessage(request, status, response.data.utf8String))

  private def infoMessage(request: HttpRequest, status: StatusCode, responseBody: String) =
    s"[OpenSkyService] Request sent to OpenSky: $request \n OpenSky response status: $status and body: $responseBody"

}

