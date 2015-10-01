package models.task.impl

import com.typesafe.scalalogging.LazyLogging
import models.dao.Repositories
import models.service.impl.{PushbulletServiceComponent, WeatherServiceComponent}
import models.task.Task
import models.value.{Registration, Weather, Venue}
import scalikejdbc.DB

import scala.concurrent.Future
import scala.util.{Failure, Try}

/**
 * Created by ynakashima on 15/05/27.
 */
trait PeriodicQueryTaskComponent {
  self: PushbulletServiceComponent with WeatherServiceComponent with Repositories =>
  import com.github.nscala_time.time.Implicits._

  val periodicQueryTask: Task
  class PeriodicQueryTaskImpl extends Task with LazyLogging {
    import play.api.libs.concurrent.Execution.Implicits._

    override def run(): Unit = Try {
      logger.info("start PeriodicQueryTask")

      val vs = DB readOnly { implicit s =>
        venueRepository.listAll()
      }

      val rfs = weatherService.getRainfall(vs)
      for {
        (v, msg) <- getNotifications(rfs)
        Registration(u, _) <- DB readOnly { implicit s => registrationRepository.findByVenueId(v.venueId) }
      } yield Future {
        pushbulletService.pushNote(s"Weather Changed ${v.name}", msg)(u.oauthToken)
      }
      update(rfs)

      logger.info("finish PeriodicQueryTask")
    } match {
      case Failure(e) => logger.error("PeriodicQueryTask was aborted", e)
      case _ =>
    }

    private trait RainVariation
    private case object Other extends RainVariation
    private case object StartToRain extends RainVariation
    private case object StopRaining extends RainVariation

    def getNotifications(rfs: Map[Venue, Weather]) = {
      for ((v, w) <- rfs) yield {
        judgeVariation(v.lastWeather, w) match {
          case StartToRain => Some(v -> s"Starting to rain at ${v.name}")
          case StopRaining => Some(v -> s"Stop raining at ${v.name}")
          case _ => None
        }
      }
    }.flatten
    
    def update(rfs: Map[Venue, Weather]) =
      for((v, w) <- rfs) DB localTx { implicit s => venueRepository.updateRainfall(v.venueId, w) }

    private def judgeVariation(prev: Weather, now: Weather): RainVariation = if (prev.observationDate >= now.observationDate) {
      Other
    } else if (prev.rainfall > 0 && now.rainfall == 0) {
      StopRaining
    } else if (prev.rainfall == 0 && now.rainfall > 0) {
      StartToRain
    } else {
      Other
    }
  }
}
