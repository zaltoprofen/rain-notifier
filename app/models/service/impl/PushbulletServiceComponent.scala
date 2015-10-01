package models.service.impl

import com.typesafe.scalalogging.LazyLogging
import config.ServerConfiguration
import lib.impl.HttpDispatcherComponent
import models.service.PushbulletService
import models.value.User
import org.apache.http.client.utils.URIBuilder

import scala.util.{Failure, Success}

/**
 * Created by ynakashima on 15/05/21.
 */
trait PushbulletServiceComponent {
  self: HttpDispatcherComponent =>

  val pushbulletService: PushbulletService

  class PushbulletServiceImpl(clientConfiguration: ServerConfiguration) extends PushbulletService with LazyLogging {
    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.native.JsonMethods._

    private[this] val authorizationUrl = "https://www.pushbullet.com/authorize"
    private[this] val apiHost = "https://api.pushbullet.com"

    override def getAuthorizationUrl: String = {
      new URIBuilder(authorizationUrl)
        .addParameter("client_id", clientConfiguration.pushbulletClientId)
        .addParameter("redirect_uri", clientConfiguration.pushbulletRedirectUri)
        .addParameter("response_type", "code").build().toString
    }

    override def getAccessToken(code: String): Option[String] = {
      logger.info(s"Getting AccessToken: code=$code")
      val params = Map(
        "grant_type" -> "authorization_code",
        "client_id" -> clientConfiguration.pushbulletClientId,
        "client_secret" -> clientConfiguration.pushbulletClientSecret,
        "code" -> code
      )

      def extractAccessToken(json: JValue) = json \ "access_token" match {
        case JString(token) => Some(token)
        case _ => None
      }

      httpDispatcher.postForm(apiHost + "/oauth2/token", params) map(parse(_)) match {
        case Success(body) =>
          logger.debug(s"token request succeed: return value=$body")
          extractAccessToken(body)
        case Failure(e) =>
          logger.error("token request was failed", e)
          None
      }
    }

    override def pushNote(title: String, message: String)(oauthToken: String): Unit = {
      logger.debug(s"pushing note: title=$title, message=$message")
      val json = ("type" -> "note") ~ ("title" -> title) ~ ("body" -> message)
      val header = Map(
        "Authorization" -> s"Bearer $oauthToken",
        "Content-Type" -> "application/json"
      )
      httpDispatcher.postString(apiHost + "/v2/pushes", compact(render(json)), header) match {
        case Failure(e) => logger.error("pushing note was failed", e)
        case Success(x) => logger.debug("pushing note was succeeded")
      }
    }

    override def getIden(oauthToken: String): Option[String] = {
      logger.debug(s"getting pushbullet identity: token=$oauthToken")
      val header = Map("Authorization" -> s"Bearer $oauthToken")

      def extractIden(json: JValue) = json \ "iden" match {
        case JString(iden) => Some(iden)
        case _ => None
      }

      httpDispatcher.getForm(apiHost + "/v2/users/me", headers=header).map(parse(_)) match {
        case Success(body) =>
          logger.debug(s"getting identity was succeeded: $body")
          extractIden(body)
        case Failure(e) =>
          logger.error("getting identity was failed", e)
          None
      }
    }
  }
}
