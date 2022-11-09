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
import slick.jdbc.JdbcProfile

import com.softwaremill.macwire._

import model.ViewValueHome
import lib.model.Todo
import lib.model.Category
import lib.persistence._

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents, val categoryRepos: CategoryRepository[_ <: JdbcProfile], val todoRepos: TodoRepository[_ <: JdbcProfile])
    extends BaseController with play.api.i18n.I18nSupport {

  /**
    * GET /todo/list
    */
  def list() = Action async { implicit req =>
    val t = todoRepos.all
    for {
      categorys <- categoryRepos.all
      todos <- t
    } yield Ok(views.html.todo.list(todos.map(_.v), categorys.map(_.v), ViewValueHome.vv_list))
  }
  
  /**
    * GET /todo/:id
    */
  def edit(id: Long) = Action async { implicit req =>
    for {
      todo_opt <- todoRepos.get(Todo.Id(id))
      category_seq <- categoryRepos.all
    }yield {
      todo_opt match {
        case Some(todo) => {
          val filledForm = TodoFormData.apply(todo)
          Ok(views.html.todo.editor(todo.id, filledForm, category_seq.map(_.v), ViewValueHome.vv_edit))
        }
        case None       => NotFound(views.html.error.page404())
      }
    }
  }

  /**
   * GET /todo/new
  */
  def register() = Action async {implicit req =>
    for {
      categorys <- categoryRepos.all
    } yield {
      val initForm = TodoFormData.init_form
      Ok(views.html.todo.register(initForm, categorys.map(_.v), ViewValueHome.vv_edit))
    }
  }

  /**
    * POST /todo/:id/update
    */
  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    TodoFormData.form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        for {
          category_seq <- categoryRepos.all
        }yield {
          BadRequest(views.html.todo.editor(id, formWithErrors, category_seq.map(_.v), ViewValueHome.vv_edit))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBのデータをupdate
        for {
          todo_old <- todoRepos.get(Todo.Id(id))
          response <- todo_old match {
            case None => Future.successful(None)
            case Some(todo) => todoRepos.update(todo.map(_.copy( title=todoFormData.title, 
                                                        content=todoFormData.content, 
                                                        category=todoFormData.category, 
                                                        state=todoFormData.state)))
          }
        } yield {
          response match {
            case None     => NotFound(views.html.error.page404())
            case Some(_)  => Redirect(routes.TodoController.list)
          }
        }
      }
    )
  }

  /**
   * POST /todo/new
  */
  def store() = Action async {implicit req =>
    TodoFormData.form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        println(formWithErrors.toString)
        for {
          categorys <- categoryRepos.all
        } yield {
          BadRequest(views.html.todo.register(formWithErrors, categorys.map(_.v), ViewValueHome.vv_edit))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBに追加
        val todo_new = Todo.apply(todoFormData.title, todoFormData.content, category=todoFormData.category)
        for {
          id <- todoRepos.add(todo_new)
        } yield {
          Redirect(routes.TodoController.list)
        }
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action async { implicit request: Request[AnyContent] =>
    // requestから直接値を取得する
    val id = for {
      body_map <- request.body.asFormUrlEncoded
      id_str_opt <- body_map.get("id")
      id_str <- id_str_opt.headOption
      id <- Try { id_str.toLong } toOption
    } yield (id)

    id match {
      case Some(id) => {
        for {
          old <- todoRepos.remove(Todo.Id(id))
        } yield {
          old match {
            case None => Redirect(routes.TodoController.list()) //500errorに置き換え
            case Some(_) => Redirect(routes.TodoController.list())
          }
        }
      }
      case None     => Future.successful(BadRequest("Invalid id"))
    }
  }
}
