/**
 *
 * to do sample project
 *
 */

package controllers

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure, Try}
import scala.concurrent.duration._

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import com.softwaremill.macwire._

import model.ViewValueHome
import lib.model.Todo
import lib.model.Category
import lib.persistence._
import views.html.defaultpages.notFound
import json.writes._
import json.reads._

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents, val categoryRepos: CategoryRepository[_ <: JdbcProfile], val todoRepos: TodoRepository[_ <: JdbcProfile])
    extends BaseController with play.api.i18n.I18nSupport {

  /**
    * GET /api/todo/list
    */
  def list() = Action async { implicit req =>
    val t = todoRepos.all
    for {
      categorys <- categoryRepos.all
      todos <- t
    } yield {
      val jsValue = todos.map(JsValueTodo.apply(_))
      Ok(Json.toJson(jsValue))
    }
  }

  /**
    * GET /api/todo/:id
    *
    * @param id
    * @return
    */
  def get(id: Long) = Action async { implicit req =>
    for{
      todo_opt <- todoRepos.get(Todo.Id(id))
    } yield {
      todo_opt match{
        case None => NotFound("Invalid id")
        case Some(todo) => {
          val jsValue = JsValueTodo.apply(todo)
          Ok(Json.toJson(jsValue))
        }
      }
    }
  }

  /**
    * POST /api/todo/:id/update
    */
  def update(id: Long) = Action(parse.json) async { implicit request =>
    request.body.validate[JsValueCreateTodo].fold(
      //パースと変換がうまくいかなかった時
      errors => {
        //TODO: どうするのが良いか考える
        Future.successful(BadRequest("Request data is unacceptable"))
      },
      //うまく行った時
      todoData => {
        //DBのデータをupdate
        for {
          todo_old <- todoRepos.get(Todo.Id(id))
          category_req <- categoryRepos.get(Category.Id(todoData.category))
          response <- todo_old match {
            case None => Future.successful(None)
            case Some(todo) => {
              category_req match {
                case None => Future.successful(None)
                case Some(_) => {
                  todoRepos.update(todo.map(_.copy( title=todoData.title, 
                                                    content=todoData.content, 
                                                    category=Category.Id(todoData.category), 
                                                    state=Todo.Status.apply(todoData.state))))
                }
              }
            }
          }
        } yield {
          //TODO: 要調査
          response match {
            case None     => BadRequest("Requested todo or category is not exist")
            case Some(_)  => Ok("updated successfully")
          }
          //Ok(ダミー)とか？
        }
      }
    )
  }

  /**
   * POST /api/todo/new
  */
  def store() = Action(parse.json) async {implicit req =>
    req.body.validate[JsValueCreateTodo].fold(
      //パースと変換がうまくいかなかった時
      errors => {
        //TODO: どうするのがいいか考える
        Future.successful(BadRequest("Request data is unacceptable"))
      },
      //うまく行った時
      todoData => {
        //DBに追加
        for {
          category_req <- categoryRepos.get(Category.Id(todoData.category))
          id <- category_req match {
            case None => Future.successful(Todo.Id(-1))
            case Some(value) => {
              val todo_new = Todo.apply(todoData.title, todoData.content, Category.Id(todoData.category))
              todoRepos.add(todo_new)
            }
          }
        } yield {
          if(id < 0) BadRequest("Reqested category is not exist")
          else Ok("Stored successfully")
        }
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete(id: Long) = Action async { implicit request: Request[AnyContent] =>
    for {
      old <- todoRepos.remove(Todo.Id(id))
    } yield {
      old match {
        case None => NotFound("Invalid id")
        case Some(_) => Ok("Deleted successfully")
      }
    }
  }
}
