package flightsaggregator.repository

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import flightsaggregator.opensky.domain.FlightState

import scala.concurrent.Future

case class FlightStateModel(icao24: String, originCountry: String, timePosition: Option[BigDecimal], onGround: Boolean)

class FlightStates extends CassandraTable[FlightStatesWithConnector, FlightStateModel] {
  object icao24 extends StringColumn(this) with PartitionKey[String]
  object originCountry extends StringColumn(this) with PartitionKey[String]
  object timePosition extends OptionalBigDecimalColumn(this) with PartitionKey[Option[BigDecimal]]
  object onGround extends BooleanColumn(this) with PartitionKey[Boolean]

  def fromRow(row: Row): FlightStateModel = {
    FlightStateModel(
      icao24        = icao24(row),
      originCountry = originCountry(row),
      timePosition  = timePosition(row),
      onGround      = onGround(row)
    )
  }
}

abstract class FlightStatesWithConnector extends FlightStates with RootConnector {
  def store(flightState: FlightState): Future[Boolean] = {
    insert
      .value(_.icao24, flightState.icao24)
      .value(_.originCountry, flightState.originCountry)
      .value(_.timePosition, flightState.timePosition)
      .value(_.onGround, flightState.onGround)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future().map(_.wasApplied())
  }
}