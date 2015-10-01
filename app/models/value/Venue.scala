package models.value

import org.joda.time.DateTime

/**
 * Created by ynakashima on 15/05/18.
 */

case class Venue(venueId: Long,
                 name: String,
                 latitude: Double,
                 longitude: Double,
                 lastRainfall: Float,
                 lastUpdate: DateTime){
  def lastWeather = Weather(lastRainfall, lastUpdate)
}
