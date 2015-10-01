package config

import com.typesafe.config.Config

/**
 * Created by ynakashima on 15/05/21.
 */
class ServerConfiguration(conf: Config) {
  val pushbulletClientId = conf.getString("pushbullet.clientId")
  val pushbulletClientSecret = conf.getString("pushbullet.clientSecret")
  val pushbulletRedirectUri = conf.getString("pushbullet.redirectUri")

  lazy val yahooAppId = conf.getString("yahoo.appId")
}
