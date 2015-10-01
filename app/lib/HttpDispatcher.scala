package lib

import java.io.InputStream

import scala.util.Try

/**
 * Created by ynakashima on 15/05/22.
 */
trait HttpDispatcher {
  def getForm(url: String, params: Map[String, String] = Map(),
              headers: Map[String, String] = Map(), charset: String = "UTF-8"): Try[String]
  def postForm(url: String, params: Map[String, String] = Map(),
               headers: Map[String, String] = Map(), charset: String = "UTF-8"): Try[String]

  def postString(url: String, payload: String,
           headers: Map[String, String] = Map(), charset: String = "UTF-8"): Try[String]
  def post(url: String, payload: InputStream,
           headers: Map[String, String] = Map(), charset: String = "UTF-8"): Try[String]
}
