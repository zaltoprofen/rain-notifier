package models.service

import models.value.User

import scala.util.Try

/**
 * Created by ynakashima on 15/05/21.
 */
trait PushbulletService {
  def getAuthorizationUrl: String
  def getAccessToken(code: String): Option[String]
  def pushNote(title: String, message: String)(oauthToken: String): Unit
  def getIden(oauthToken: String): Option[String]
}
