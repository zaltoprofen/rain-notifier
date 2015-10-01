import akka.actor.Props
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import config.ServerConfiguration
import controllers.{UserContentControllerComponent, UserContentController, AuthorizationController, AuthorizationControllerComponent}
import lib.HttpDispatcher
import lib.impl.HttpDispatcherComponent
import models.dao.Repositories
import models.service.PushbulletService
import models.service.impl.{PushbulletServiceComponent, WeatherServiceComponent}
import models.task.TaskExecutor
import models.task.impl.PeriodicQueryTaskComponent
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.{Application, GlobalSettings}

/**
 * Created by ynakashima on 15/05/26.
 */

trait RealWorld
  extends AuthorizationControllerComponent
  with UserContentControllerComponent
  with PeriodicQueryTaskComponent
  with PushbulletServiceComponent
  with HttpDispatcherComponent
  with WeatherServiceComponent
  with Repositories

object Global extends GlobalSettings with RealWorld {

  override def onStart(application: Application) = {
    scalikejdbc.config.DBs.setupAll()

    val scheduler = QuartzSchedulerExtension(Akka.system)
    scheduler.schedule(
      "PeriodicQueryTask",
      Akka.system.actorOf(Props(classOf[TaskExecutor], periodicQueryTask)),
      "periodic query"
    )
  }

  override def getControllerInstance[A](clazz: Class[A]): A = {
    if (clazz == classOf[AuthorizationController]) authorizationController.asInstanceOf[A]
    else if (clazz == classOf[UserContentController]) userContentController.asInstanceOf[A]
    else clazz.newInstance()
  }

  // Inject Libs
  val connectionManager = new PoolingHttpClientConnectionManager()
  connectionManager.setMaxTotal(10)
  override val httpDispatcher: HttpDispatcher = new HttpDispatcherImpl(connectionManager)

  // Inject Services
  val conf = new ServerConfiguration(ConfigFactory.load())
  override val pushbulletService: PushbulletService = new PushbulletServiceImpl(conf)
  override val weatherService = new YahooWeatherServiceImpl(conf)

  // Inject Repositories
  override val userRepository = new UserRepositoryImpl()
  override val registrationRepository = new RegistationRepositoryImpl()
  override val venueRepository = new VenueRepositoryImpl()

  // Inject Controllers
  override val authorizationController: AuthorizationController = new AuthorizationControllerImpl()
  override val userContentController = new UserContentControllerImpl()

  override val periodicQueryTask = new PeriodicQueryTaskImpl()
}
