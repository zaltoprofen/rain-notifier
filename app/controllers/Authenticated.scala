package controllers

import com.typesafe.scalalogging.LazyLogging
import models.dao.UserRepository
import models.value.User
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.mvc._
import scalikejdbc.DB

import scala.util.Try

/**
 * Created by ynakashima on 15/05/28.
 */

trait Authenticated extends LazyLogging {
  self: Controller =>

  import com.github.nscala_time.time.Implicits._

  private[this] val sessionCreatedTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss")

  def authenticate(req: RequestHeader) = {
    logger.debug(s"authenticating: session=${req.session}")
    val now = new DateTime
    for {
      ctimeStr <- req.session.get("ctime")
      ctime <- Try(sessionCreatedTimeFormat.parseDateTime(ctimeStr)).toOption
      uname <- req.session.get(Security.username)
      if ctime + 10.minutes > now
    } yield uname
  }

  def onUnauthorized(req: RequestHeader) = {
    logger.debug(s"reject auth: session=${req.session}")
    Redirect(routes.Application.index)
      .withNewSession.flashing("message" -> "Unauthenticated")
  }

  def withAuth(f: String => Request[AnyContent] => Result) = {
    Security.Authenticated(authenticate, onUnauthorized) { user =>
      Action(req => f(user)(req))
    }
  }

  def withUser(userRepository: UserRepository)(f: User => Request[AnyContent] => Result) = {
    withAuth { user => req =>
      DB readOnly { implicit s => userRepository.findById(user) } match {
        case Some(u) => f(u)(req)
        case None => onUnauthorized(req)
      }
    }
  }

  def newSession(result: Result, userId: String) = {
    val now = new DateTime
    result.withSession(Security.username -> userId, "ctime" -> sessionCreatedTimeFormat.print(now))
  }
}