package models.dao

import models.value.{Weather, Venue}
import org.joda.time.DateTime
import scalikejdbc.DBSession



trait VenueRepository {
  def listAll()(implicit session: DBSession): List[Venue]
  def exist(venueId: Long)(implicit session: DBSession): Boolean
  def updateRainfall(venueId: Long, lastWeather: Weather)(implicit session: DBSession): Boolean
}