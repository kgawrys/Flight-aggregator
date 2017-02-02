package flightsaggregator.core

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import flightsaggregator.PollingActor
import flightsaggregator.PollingActor.Poll
import flightsaggregator.kafka._
import flightsaggregator.opensky.OpenSkyService
import flightsaggregator.opensky.domain.{OpenSkyConfig, OpenSkyHost}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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
    openSkyHost = OpenSkyHost(config.getString("services.opensky.host"))
  )

  lazy val kafkaConfig = KafkaConfig(
    kafkaHost  = KafkaHost(config.getString("kafka.host")),
    kafkaPort  = KafkaPort(config.getInt("kafka.port")),
    stateTopic = KafkaTopic(config.getString("kafka.topics.statetopic"))
  )

  lazy val kafkaProducer: KafkaProducer = wire[KafkaProducer]
  lazy val openSkyService: OpenSkyService = wire[OpenSkyService]
}

object Main extends App with Setup {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val pollFlightsActor = system.actorOf(Props(new PollingActor(logger, openSkyService, kafkaProducer, kafkaConfig)))

  system.scheduler.schedule(0 seconds, 30 seconds, pollFlightsActor, Poll)

}
