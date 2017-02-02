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
import flightsaggregator.opensky.domain.{OpenSkyConfig, OpenSkyStatesRequest, OpenSkyStatesResponse}
import spray.json.RootJsonFormat

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object OpenSkyService {
  type OpenSkyResponse[T] = Either[Error, T]
  implicit val openSkyStatesResponseFormat = jsonFormat2(OpenSkyStatesResponse.apply)
}

class OpenSkyService(config: OpenSkyConfig, logger: LoggingAdapter)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer) {

  import OpenSkyService._

  private val host = config.openSkyHost.host

  def getStates(request: OpenSkyStatesRequest): Future[OpenSkyResponse[OpenSkyStatesResponse]] = {
    val statesRequest = buildStatesRequest(request)
    sendRequestToOpenSky[OpenSkyStatesResponse](statesRequest, "Couldn't retrieve OpenSky states.")
  }

  private def buildStatesRequest(request: OpenSkyStatesRequest) = RequestBuilding.Get(Uri(s"$host/states/all"))

  private def sendRequestToOpenSky[T](request: HttpRequest, failureMessage: String)(implicit conversion: RootJsonFormat[T]) = {
    logger.debug("[OpenSkyClientService] request sent to Slack {} ", request)
    Http().singleRequest(request).flatMap { response =>
      response.entity.toStrict(5.seconds).flatMap { entity =>
        logRequestResponse(request, response.status, entity)
        response.status match {
          case OK => Unmarshal(entity).to[T].map(Right(_))
          case _  => Future.successful(Left(Error(failureMessage)))
        }
      }
    }
  }

  private def logRequestResponse(request: HttpRequest, status: StatusCode, response: HttpEntity.Strict): Unit =
    logger.info(infoMessage(request, status, response.data.utf8String))

  private def infoMessage(request: HttpRequest, status: StatusCode, responseBody: String) =
    s"[OpenSkyService] Request sent to OpenSky: $request \n OpenSky response status: $status and body: $responseBody"

}

