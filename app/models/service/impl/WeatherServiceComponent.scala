package models.service.impl

import com.typesafe.scalalogging.LazyLogging
import config.ServerConfiguration
import lib.impl.HttpDispatcherComponent
import models.service.WeatherService
import models.value.{Weather, Venue}
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success}

/**
 * Created by ynakashima on 15/05/27.
 */
trait WeatherServiceComponent {
  self: HttpDispatcherComponent =>

  val weatherService: WeatherService
  class YahooWeatherServiceImpl(serverConfiguration: ServerConfiguration) extends WeatherService with LazyLogging {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    private[this] val endpoint = "http://weather.olp.yahooapis.jp/v1/place"
    private[this] val dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmm")

    private def extractRainfall(json: JValue): List[Option[Weather]] = {
      val JArray(fs) = json \ "Feature"
      fs.map(_ \ "Property" \ "WeatherList" \ "Weather").collect {case JArray(ws) =>
        (ws.head \ "Rainfall", ws.head \ "Date") match {
          case (JDouble(rf), JString(t)) => Some(Weather(rf.toFloat, dateFormat.parseDateTime(t)))
          case _ => None
        }
      }
    }

    override def getRainfall(venue: Venue): Option[Weather] =
      getRainfall(List(venue)).values.headOption

    override def getRainfall(venues: List[Venue]): Map[Venue, Weather] = {
      @annotation.tailrec
      def go(residue: List[Venue], complete: Map[Venue, Weather]): Map[Venue, Weather] = {
        val (l, r) = residue.splitAt(10)

        val coordinates = l.map(v => s"${v.longitude},${v.latitude}").mkString(" ")
        val params = Map(
          "coordinates" -> coordinates,
          "appid" -> serverConfiguration.yahooAppId,
          "output" -> "json",
          "interval" -> "5"
        )
        logger.debug(s"requested weather query params: $params")

        val c = httpDispatcher.getForm(endpoint, params).map(parse(_)) match {
          case Success(body) =>
            logger.debug(s"returned from yahoo api: ${pretty(render(body))}")
            val rainfalls = extractRainfall(body)
            complete ++ Map(venues.zip(rainfalls).collect{case (v, Some(weather)) => v -> weather} :_*)
          case Failure(e) =>
            logger.error("could not get", e)
            complete
        }
        if (r.isEmpty){
          logger.debug(s"parsed response from yahoo api: $c")
          c
        }
        else go(r, c)
      }

      go(venues, Map())
    }
  }
}
