package models.dao

import models.dao.impl.{RegistrationRepositoryComponent, VenueRepositoryComponent, UserRepositoryComponent}

/**
 * Created by ynakashima on 15/05/27.
 */
trait Repositories
  extends UserRepositoryComponent
  with VenueRepositoryComponent
  with RegistrationRepositoryComponent
