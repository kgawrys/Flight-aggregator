import sbt._

object Version {
  val akkaV        = "2.4.10"
  val logbackV     = "1.1.9"
  val macwireV     = "2.2.5"
  val scalaTestV   = "3.0.1"
}

object Library {
  import Version._

  val akkaActor        = "com.typesafe.akka"         %% "akka-actor"                        % akkaV
  val akkaSfl4j        = "com.typesafe.akka"         %% "akka-slf4j"                        % akkaV
  val akkaStream       = "com.typesafe.akka"         %% "akka-stream"                       % akkaV
  val akkaHttpCore     = "com.typesafe.akka"         %% "akka-http-core"                    % akkaV
  val akkaHttp         = "com.typesafe.akka"         %% "akka-http-experimental"            % akkaV
  val akkaSprayJson    = "com.typesafe.akka"         %% "akka-http-spray-json-experimental" % akkaV

  // macwire
  val macwireMacros    = "com.softwaremill.macwire"  %% "macros"                            % macwireV
  val macwireUtil      = "com.softwaremill.macwire"  %% "util"                              % macwireV

  // other
  val logback          = "ch.qos.logback"            % "logback-classic"                    % logbackV
  val scalatest        =  "org.scalatest"            %% "scalatest"                         % scalaTestV
}

object Dependencies {
  import Library._

  val apiCore = Seq(
    akkaActor, akkaSfl4j, akkaStream, akkaHttpCore, akkaHttp, akkaSprayJson,
    logback,
    scalatest,
    macwireMacros % "provided",
    macwireUtil
  )
}
