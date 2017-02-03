package flightsaggregator.core

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import com.websudos.phantom.connectors.ContactPoint
import flightsaggregator.PollingActor
import flightsaggregator.PollingActor.Poll
import flightsaggregator.aggregator.AggregatorService
import flightsaggregator.kafka._
import flightsaggregator.opensky.OpenSkyService
import flightsaggregator.opensky.domain.{OpenSkyConfig, OpenSkyHost}
import flightsaggregator.repository.CassandraConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class ServerConfig(interface: String, port: Int, hostname: String)
case class AppConfig(pollInterval: Int, windowInterval: Int)

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
    kafkaHostname = KafkaHostname(config.getString("kafka.host")),
    kafkaPort     = KafkaPort(config.getInt("kafka.port")),
    stateTopic    = KafkaTopic(config.getString("kafka.topics.statetopic"))
  )

  lazy val appConfig = AppConfig(
    pollInterval   = config.getInt("app.pollinterval"),
    windowInterval = config.getInt("app.windowinterval")
  )

  lazy val cassandraConfig = CassandraConfig(
    hostname                = config.getString("cassandra.hostname"),
    port                    = config.getInt("cassandra.port"),
    replicationStrategy     = config.getString("cassandra.replication-strategy"),
    replicationFactor       = config.getString("cassandra.replication-factor"),
    defaultConsistencyLevel = config.getString("cassandra.default-consistency-level"),
    keyspace                = config.getString("cassandra.keyspace")
  )

  //  val hosts = config.getStringList("cassandra.hosts").asScala.toSeq
  val Connector = ContactPoint.apply("172.17.0.2", 9042).keySpace("flights") // todo get from conf
  //  val connector = ContactPoints.apply(hosts).keySpace(config.getString("cassandra.keyspace"))
  //  val db = new Database(connector)

  lazy val kafkaProducer: KafkaProducer = wire[KafkaProducer]
  lazy val kafkaConsumer: KafkaConsumer = wire[KafkaConsumer]
  lazy val openSkyService: OpenSkyService = wire[OpenSkyService]
  lazy val aggregatorService: AggregatorService = wire[AggregatorService]
}

object Main extends App with Setup {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val pollFlightsActor = system.actorOf(Props(new PollingActor(logger, openSkyService, kafkaProducer, kafkaConfig)))
  system.scheduler.schedule(0 seconds, appConfig.pollInterval seconds, pollFlightsActor, Poll)

  aggregatorService.graph.run()
}
