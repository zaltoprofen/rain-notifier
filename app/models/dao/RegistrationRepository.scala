package models.dao

import models.value.Registration
import scalikejdbc.DBSession



trait RegistrationRepository {
  def listAll()(implicit session: DBSession): List[Registration]
  def findByUserId(userId: String)(implicit session: DBSession): List[Registration]
  def findByVenueId(venueId: Long)(implicit session: DBSession): List[Registration]
  def register(userId: String, venueId: Long)(implicit session: DBSession): Boolean
  def delete(userId: String, venueId: Long)(implicit session: DBSession): Boolean
}
