package models.dao.impl

import models.dao.RegistrationRepository
import models.value.{Venue, User, Registration}
import scalikejdbc._

/**
 * Created by ynakashima on 15/05/27.
 */
trait RegistrationRepositoryComponent {
  val registrationRepository: RegistrationRepository
  class RegistationRepositoryImpl extends RegistrationRepository {
    def rsToRegistration(rs: WrappedResultSet) =
      Registration(
        User(rs.string("u.id"), rs.string("u.oauth_token")),
        Venue(
          rs.long("v.id"),
          rs.string("v.venue_name"),
          rs.double("v.latitude"),
          rs.double("v.longitude"),
          rs.float("v.last_rainfall"),
          rs.jodaDateTime("v.last_update")
        )
      )

    override def listAll()(implicit session: DBSession): List[Registration] =
      sql"""SELECT * FROM registrations AS r
           | INNER JOIN users AS u ON r.user_id = u.id
           | INNER JOIN venues AS v ON r.venue_id = v.id""".stripMargin.map(rsToRegistration).list().apply()

    override def findByVenueId(venueId: Long)(implicit session: DBSession): List[Registration] =
      sql"""SELECT * FROM registrations AS r
           | INNER JOIN users AS u ON r.user_id = u.id
           | INNER JOIN venues AS v ON r.venue_id = v.id
           | WHERE v.id = $venueId""".stripMargin.map(rsToRegistration).list().apply()

    override def findByUserId(userId: String)(implicit session: DBSession): List[Registration] =
      sql"""SELECT * FROM registrations AS r
           | INNER JOIN users AS u ON r.user_id = u.id
           | INNER JOIN venues AS v ON r.venue_id = v.id
           | WHERE u.id = $userId""".stripMargin.map(rsToRegistration).list().apply()

    override def delete(userId: String, venueId: Long)(implicit session: DBSession): Boolean =
        sql"DELETE FROM registrations WHERE user_id = $userId AND venue_id = $venueId".update().apply() == 1

    override def register(userId: String, venueId: Long)(implicit session: DBSession): Boolean =
        sql"INSERT INTO registrations (user_id, venue_id) VALUE ($userId, $venueId)".update().apply() == 1

  }
}
