import sbt._

object Version {
  val akkaV           = "2.4.10"
  val akkaHttpV       = "2.4.10"
  val logbackV        = "1.1.9"
  val macwireV        = "2.2.5"
  val scalaTestV      = "3.0.1"
  val reactiveKafkaV  = "0.13"
}

object Library {
  import Version._

  // akka
  val akkaActor        = "com.typesafe.akka"         %% "akka-actor"                        % akkaV
  val akkaSfl4j        = "com.typesafe.akka"         %% "akka-slf4j"                        % akkaV
  val akkaStream       = "com.typesafe.akka"         %% "akka-stream"                       % akkaV
  val reactiveKafka    = "com.typesafe.akka"         %% "akka-stream-kafka"                 % reactiveKafkaV

  // http
  val akkaHttpCore     = "com.typesafe.akka"         %% "akka-http-core"                    % akkaHttpV
  val akkaHttp         = "com.typesafe.akka"         %% "akka-http-experimental"            % akkaHttpV
  val akkaSprayJson    = "com.typesafe.akka"         %% "akka-http-spray-json-experimental" % akkaHttpV
  val akkaHttpTestkit  = "com.typesafe.akka"         %% "akka-http-testkit"                 % akkaHttpV

  // macwire
  val macwireMacros    = "com.softwaremill.macwire"  %% "macros"                            % macwireV
  val macwireUtil      = "com.softwaremill.macwire"  %% "util"                              % macwireV

  // other
  val logback          = "ch.qos.logback"            % "logback-classic"                    % logbackV
  val scalatest        = "org.scalatest"             %% "scalatest"                         % scalaTestV
}

object Dependencies {
  import Library._

  val apiCore = Seq(
    akkaActor, akkaSfl4j, akkaStream, akkaHttpCore, akkaHttp, akkaHttpTestkit, akkaSprayJson,
    reactiveKafka,
    logback,
    scalatest,
    macwireMacros % "provided",
    macwireUtil
  )
}
