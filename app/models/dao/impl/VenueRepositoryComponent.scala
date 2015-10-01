package models.dao.impl

import com.typesafe.scalalogging.LazyLogging
import models.dao.VenueRepository
import models.value.{Weather, Venue}
import org.joda.time.DateTime
import scalikejdbc._

/**
 * Created by ynakashima on 15/05/21.
 */
trait VenueRepositoryComponent {

  val venueRepository: VenueRepository

  class VenueRepositoryImpl extends VenueRepository with LazyLogging {
    private def rsToVenue(rs: WrappedResultSet) =
      Venue(
        rs.long("id"),
        rs.string("venue_name"),
        rs.double("latitude"),
        rs.double("longitude"),
        rs.float("last_rainfall"),
        rs.jodaDateTime("last_update")
      )

    override def listAll()(implicit session: DBSession): List[Venue] = {
      logger.debug("listing venueRepository")
      sql"""select id, venue_name, latitude, longitude, last_rainfall, last_update from venues""".map(rsToVenue).toList().apply()
    }

    override def updateRainfall(venueId: Long, lastWeather: Weather)(implicit session: DBSession): Boolean = {
      logger.debug(s"updating venue rainfall: venueId=$venueId, lastWeather=$lastWeather")
      val Weather(lastRainfall, lastUpdate) = lastWeather
      sql"""UPDATE venues SET last_rainfall=$lastRainfall, last_update=$lastUpdate WHERE id=$venueId""".update().apply() == 1
    }

    override def exist(venueId: Long)(implicit session: DBSession): Boolean = {
      logger.debug(s"searching venue: venueId=$venueId")
      sql"""SELECT COUNT(*) FROM venues WHERE id = $venueId""".map(_.long(1)).single().apply() match {
        case Some(c) => c == 1
        case None =>
          logger.error("count query returns no culumn")
          false
      }
    }
  }
}
