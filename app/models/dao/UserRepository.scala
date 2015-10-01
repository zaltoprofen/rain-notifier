package models.dao

import models.value.User
import scalikejdbc.DBSession



trait UserRepository {
  def listAll()(implicit ctx: DBSession): List[User]
  def findById(id: String)(implicit ctx: DBSession): Option[User]
  def findByOAuthToken(oauthToken: String)(implicit ctx: DBSession): Option[User]
  def deleteByOAuthToken(oauthToken: String)(implicit ctx: DBSession): Unit
  def deleteById(id: String)(implicit ctx: DBSession): Unit
  def upsert(id: String, oauthToken: String)(implicit ctx: DBSession): Unit
}
