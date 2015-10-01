package models.service

import com.typesafe.config.ConfigFactory
import controllers.{AuthorizationController, AuthorizationControllerComponent}
import lib.HttpDispatcher
import lib.impl.HttpDispatcherComponent
import models.dao.impl.{UserRepositoryComponent, VenueRepositoryComponent}
import models.dao.{UserRepository, VenueRepository}
import models.service.impl.PushbulletServiceComponent
import org.specs2.mock.Mockito

/**
 * Created by ynakashima on 15/05/27.
 */
trait TestEnvironment
  extends AuthorizationControllerComponent
    with PushbulletServiceComponent
    with HttpDispatcherComponent
    with UserRepositoryComponent
    with VenueRepositoryComponent
    with Mockito {

  override val authorizationController = mock[AuthorizationController]
  override val pushbulletService = mock[PushbulletService]
  override val httpDispatcher = mock[HttpDispatcher]
  override val userRepository = mock[UserRepository]
  override val venueRepository = mock[VenueRepository]
}
