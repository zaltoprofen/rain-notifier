package lib.impl

import java.io._

import com.typesafe.scalalogging.LazyLogging
import lib.HttpDispatcher
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpUriRequest}
import org.apache.http.client.utils.{HttpClientUtils, URIBuilder}
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.entity.{BasicHttpEntity, StringEntity}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpHost, NameValuePair}

import scala.util.Try

/**
 * Created by ynakashima on 15/05/22.
 */
trait HttpDispatcherComponent {

  val httpDispatcher: HttpDispatcher

  class HttpDispatcherImpl(connectionManager: HttpClientConnectionManager) extends HttpDispatcher with LazyLogging {
    private[this] val clientBuilder =
      HttpClientBuilder.create().setConnectionManager(connectionManager)

    private def request(charset: String)(f: => HttpUriRequest): Try[String] = Try {
      val c = clientBuilder.build()
      val res = c.execute(f)
      try {
        EntityUtils.toString(res.getEntity, charset)
      } finally {
        EntityUtils.consume(res.getEntity)
      }
    }

    override def getForm(url: String,
                         params: Map[String, String],
                         headers: Map[String, String],
                         charset: String): Try[String] = request(charset) {
      logger.debug(s"GET: url=$url, params=$params")
      val uriBuilder = new URIBuilder(url)
      for ((name, value) <- params) uriBuilder.addParameter(name, value)
      val method = new HttpGet(uriBuilder.build().toString.replace("+", "%20"))
      for ((name, value) <- headers) method.addHeader(name, value)
      method
    }

    override def postForm(url: String,
                          params: Map[String, String],
                          headers: Map[String, String],
                          charset: String): Try[String] = request(charset) {
      logger.debug(s"POST: url=$url, params=$params")
      val method = new HttpPost(url)
      val ps: java.util.List[NameValuePair] = new java.util.ArrayList()
      for ((name, value) <- headers) method.addHeader(name, value)
      for ((name, value) <- params) ps.add(new BasicNameValuePair(name, value))
      method.setEntity(new UrlEncodedFormEntity(ps))
      method
    }

    override def post(url: String,
                      payload: InputStream,
                      headers: Map[String, String],
                      charset: String): Try[String] = request(charset) {
      logger.debug(s"POST: url=$url, data is passed by InputStream")
      val method = new HttpPost(url)
      for ((name, value) <- headers) method.addHeader(name, value)
      val content = new BasicHttpEntity()
      content.setContent(payload)
      method.setEntity(content)
      method
    }

    override def postString(url: String,
                      payload: String,
                      headers: Map[String, String],
                      charset: String): Try[String] = request(charset) {
      logger.debug(s"POST: url=$url data=$payload")
      val method = new HttpPost(url)
      for ((name, value) <- headers) method.addHeader(name, value)
      method.setEntity(new StringEntity(payload, "UTF-8"))
      method
    }
  }

}
