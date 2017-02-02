package flightsaggregator.core

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import flightsaggregator.opensky.OpenSkyService
import flightsaggregator.opensky.domain.{OpenSkyConfig, OpenSkyHost}

import scala.concurrent.ExecutionContext

case class ServerConfig(interface: String, port: Int, hostname: String)

trait Setup {
  import com.softwaremill.macwire._

  implicit val system: ActorSystem
  implicit def executor: ExecutionContext
  implicit val materializer: Materializer

  lazy val logger = Logging(system, getClass)
  lazy val config = ConfigFactory.load()

  lazy val serverConfig = ServerConfig(
    interface = config.getString("http.interface"),
    port      = config.getInt("http.port"),
    hostname  = config.getString("http.hostname")
  )

  lazy val openSkyConfig = OpenSkyConfig(
    host = OpenSkyHost(config.getString("services.open-sky.host"))
  )

  lazy val openSkyService: OpenSkyService = wire[OpenSkyService]
}

object Main extends App with Setup {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

}
