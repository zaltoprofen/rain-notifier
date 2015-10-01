package models.service

import java.net.URL

import com.typesafe.config.ConfigFactory
import org.junit.runner._
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import config.ServerConfiguration

import scala.util.Success

/**
 * Created by ynakashima on 15/05/27.
 */
@RunWith(classOf[JUnitRunner])
class PushbulletServiceImplSpec extends Specification with TestEnvironment {
  val config = mock[ServerConfiguration]
  config.pushbulletClientId returns "client-id"
  config.pushbulletClientSecret returns "client-secret"
  config.pushbulletRedirectUri returns "redirect-uri"
  override val pushbulletService = new PushbulletServiceImpl(config)

  "pushNote" should {
    "pass correct arguments to httpDispacher" in {
      httpDispatcher.postString(anyString, anyString, any, any) returns Success("john")

      pushbulletService.pushNote("xxx", "xxx")("token")

      there was one(httpDispatcher)
        .postString(
          ===("https://api.pushbullet.com/v2/pushes"),
          anyString,
          ===(Map("Authorization" -> "Bearer token", "Content-Type" -> "application/json")),
          any)
    }
  }

  "getAuthorizationUrl" should {
    "return correct URL" in {
      val url = new URL(pushbulletService.getAuthorizationUrl)
      url.getProtocol must_== "https"
      url.getHost must_== "www.pushbullet.com"
      url.getPath must_== "/authorize"

      val query = Map(url.getQuery.split("&").map { pair =>
        val p = pair.split("=")
        p(0) -> p(1)
      }:_*)
      query must_== Map("client_id" -> "client-id", "redirect_uri" -> "redirect-uri", "response_type" -> "code")
    }
  }
}
