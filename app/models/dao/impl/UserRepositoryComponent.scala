package models.dao.impl

import models.dao.UserRepository
import models.value.User
import scalikejdbc._

/**
 * Created by ynakashima on 15/05/18.
 */
trait UserRepositoryComponent {

  val userRepository: UserRepository

  class UserRepositoryImpl extends UserRepository {
    private def rsToUser(rs: WrappedResultSet) = User(rs.string("id"), rs.string("oauth_token"))

    override def listAll()(implicit ctx: DBSession): List[User] =
      sql"select id, oauth_token from users".map(rsToUser).list().apply()

    override def findById(id: String)(implicit ctx: DBSession): Option[User] =
      sql"select id, oauth_token from users where id = $id".map(rsToUser).single().apply()

    override def findByOAuthToken(oauthToken: String)(implicit ctx: DBSession): Option[User] =
      sql"select id, oauth_token from users where oauth_token = $oauthToken".map(rsToUser).single().apply()

    override def deleteByOAuthToken(oauthToken: String)(implicit ctx: DBSession): Unit =
      sql"delete from users where oauth_token = $oauthToken".update().apply()

    override def deleteById(id: String)(implicit ctx: DBSession): Unit =
      sql"delete from users where id = $id".update().apply()

    override def upsert(id: String, oauthToken: String)(implicit ctx: DBSession): Unit =
      sql"""insert into users (id, oauth_token) values ($id, $oauthToken)
           | on duplicate key
           | update oauth_token = $oauthToken
         """.stripMargin.update().apply()
  }

}