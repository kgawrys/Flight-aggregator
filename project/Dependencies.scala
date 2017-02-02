import sbt._

object Version {
  val akkaV        = "2.4.10"
  val logbackV     = "1.1.9"
}

object Library {
  import Version._

  val akkaActor        = "com.typesafe.akka"        %% "akka-actor"                        % akkaV
  val akkaSfl4j        = "com.typesafe.akka"        %% "akka-slf4j"                        % akkaV
  val akkaStream       = "com.typesafe.akka"        %% "akka-stream"                       % akkaV
  val akkaHttpCore     = "com.typesafe.akka"        %% "akka-http-core"                    % akkaV
  val akkaHttp         = "com.typesafe.akka"        %% "akka-http-experimental"            % akkaV
  val akkaSprayJson    = "com.typesafe.akka"        %% "akka-http-spray-json-experimental" % akkaV

  val logback          = "ch.qos.logback"            % "logback-classic"                   % logbackV
}

object Dependencies {
  import Library._

  val apiCore = Seq(
    akkaActor, akkaSfl4j, akkaStream, akkaHttpCore, akkaHttp, akkaSprayJson,
    logback
  )
}
