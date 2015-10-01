package models.task

import akka.actor.Actor

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by ynakashima on 15/05/28.
 */
class TaskExecutor(task: Task) extends Actor {
  override def receive: Receive = {
    case _ => Future { task.run() }
  }
}
