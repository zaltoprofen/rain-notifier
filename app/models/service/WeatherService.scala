package models.service

import models.value.{Venue, Weather}

/**
 * Created by ynakashima on 15/05/27.
 */
trait WeatherService {
  def getRainfall(venue: Venue): Option[Weather]
  def getRainfall(venues: List[Venue]): Map[Venue, Weather]
}


