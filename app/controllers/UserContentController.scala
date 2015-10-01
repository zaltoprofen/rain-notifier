package controllers

import com.typesafe.scalalogging.LazyLogging
import models.dao.Repositories
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import scalikejdbc.DB
import scalikejdbc.TxBoundary.Try._

import scala.util.{Failure, Success, Try}

/**
 * Created by ynakashima on 15/05/28.
 */
trait UserContentController extends Controller {
  self: Authenticated =>

  def index: EssentialAction
  def register: EssentialAction
  def delete: EssentialAction
}

trait UserContentControllerComponent {
  self: Repositories =>

  val userContentController: UserContentController
  class UserContentControllerImpl extends UserContentController with Authenticated with LazyLogging {

    override def index: EssentialAction = withUser(userRepository) { user => implicit req =>
      val (reg, unreg) = DB readOnly { implicit session =>
        val registered = registrationRepository.findByUserId(user.id).map(_.venue)
        (registered, venueRepository.listAll.filterNot(a => registered.contains(a)))
      }
      Ok(views.html.user.render(venuesForm, unreg, reg, req))
    }

    val venuesForm = Form(
      single("venue_id" -> list(longNumber))
    )

    private def checkExist(f: List[Long] => List[(String, String)])(implicit req: Request[AnyContent]): List[(String, String)] = {
      logger.debug(s"validating: ${req.body.asFormUrlEncoded}")
      venuesForm.bindFromRequest.fold (
      errorForm => {
        logger.debug(s"POST data rejected: ${errorForm.errors}")
        List("error" -> "invalid request")
      },
      {
        vids =>
          logger.debug(s"POST data accepted: $vids")
          val exists = DB readOnly { implicit session =>
            vids.nonEmpty && vids.forall(vid => venueRepository.exist(vid))
          }
          if (exists) {
            logger.debug("Posted venues are existing")
            f(vids)
          } else {
            logger.debug("Posted venues are not existing")
            List("error" -> "invalid request")
          }
      })
    }

    override def delete: EssentialAction = withUser(userRepository) { user => implicit req =>
      val redirectMsg = checkExist { vids =>
        DB localTx { implicit s =>
          logger.debug(s"Delete registration : $vids")
          Try { vids.foreach(registrationRepository.delete(user.id, _)) }
        } match {
          case Success(_) =>
            logger.debug("Succeeded to delete registrations")
            List("success" -> "complete deletion!")
          case Failure(e) =>
            logger.error("Error occurred while deleting", e)
            List("error" -> "failed to delete venues")
        }
      }
      Redirect(routes.UserContentController.index).flashing(redirectMsg: _*)
    }

    override def register = withUser(userRepository) { user => implicit req =>
      val redirectMsg = checkExist { vids =>
        logger.debug(s"Register: $vids")
        DB localTx { implicit s =>
          Try {
            vids.forall(vid => registrationRepository.register(user.id, vid))
          }
        } match {
          case Success(_) =>
            logger.debug("Succeeded to register")
            List("success" -> "complete registration!")
          case Failure(e) =>
            logger.error("Error occurred while registering", e)
            List("error" -> "failed to register venue")
        }
      }
      Redirect(routes.UserContentController.index).flashing(redirectMsg: _*)
    }
  }
}