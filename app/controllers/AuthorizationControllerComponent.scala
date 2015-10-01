package controllers

import models.dao.impl.UserRepositoryComponent
import models.service.impl.PushbulletServiceComponent
import models.value.User
import play.api.mvc.{Security, AnyContent, Controller, Action}
import scalikejdbc.DB

/**
 * Created by ynakashima on 15/05/21.
 */

trait AuthorizationController extends Controller {
  def authorize: Action[AnyContent]
  def callback(code: Option[String], error: Option[String]): Action[AnyContent]
}

trait AuthorizationControllerComponent {
  self: PushbulletServiceComponent with UserRepositoryComponent =>

  val authorizationController: AuthorizationController
  class AuthorizationControllerImpl extends AuthorizationController with Authenticated {
    override def authorize = Action {
      Ok(views.html.authorization(pushbulletService.getAuthorizationUrl))
    }

    override def callback(code: Option[String], error: Option[String]) = Action {
      val x = for {
        c <- code
        token <- pushbulletService.getAccessToken(c)
        iden <- pushbulletService.getIden(token)
        u <- DB localTx { implicit session =>
          userRepository.upsert(iden, token)
          userRepository.findById(iden)
        }
      } yield u

      x match {
        case Some(usr: User) => newSession(Redirect(routes.UserContentController.index), usr.id)
        case None => Redirect(routes.Application.index)
      }
    }
  }

}
