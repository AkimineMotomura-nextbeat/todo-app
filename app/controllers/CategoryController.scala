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
import play.api.data.validation._
import play.api.data.validation.Constraints._
import slick.jdbc.JdbcProfile

import com.softwaremill.macwire

import model.ViewValueHome
import lib.model.Category
import lib.persistence._

@Singleton
class CategoryController @Inject()(val controllerComponents: ControllerComponents, val categoryRepos: CategoryRepository[_ <: JdbcProfile], val todoRepos: TodoRepository[_ <: JdbcProfile])
    extends BaseController with play.api.i18n.I18nSupport {

  /**
    * GET /todo/category/list
    */
  def list() = Action async { implicit req =>
    for {
      categorys <- categoryRepos.all
    } yield Ok(views.html.todo.category.list(categorys.map(_.v), ViewValueHome.vv_list))
  }

  /**
    * GET /todo/category/:id
    */
  
  def edit(id: Long) = Action async { implicit req =>
    for {
      category_seq <- categoryRepos.all
    }yield {
      val category_opt = category_seq.find(_.id == id)
      
      category_opt match {
        case Some(category) => {
          val filledForm = CategoryFormData.apply(category)
          Ok(views.html.todo.category.editor(category.id, category_seq.map(_.v), filledForm, ViewValueHome.vv_edit))
        }
        case None       => NotFound(views.html.error.page404())
      }
    }
  }

  /**
   * GET /todo/category/new
  */
  def register() = Action async {implicit req =>
    for {
        category_seq <- categoryRepos.all
    } yield {
        Ok(views.html.todo.category.register(category_seq.map(_.v), CategoryFormData.form, ViewValueHome.vv_edit))
    }
  }

  /**
    * POST /todo/category/:id/update
    */
  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    CategoryFormData.form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[CategoryFormData]) => {
        for {
            category_seq <- categoryRepos.all
        } yield {
            BadRequest(views.html.todo.category.editor(id, category_seq.map(_.v), formWithErrors, ViewValueHome.vv_edit))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (categoryFormData: CategoryFormData) => {
        //DBのデータをupdate
        for {
          category_old <- categoryRepos.get(Category.Id(id))
          response <- category_old match {
            case None => Future.successful(None)
            case Some(category) => categoryRepos.update(category.map(_.copy(  name=categoryFormData.name, 
                                                                              slug=categoryFormData.slug,
                                                                              color=categoryFormData.color)))
          }
        } yield {
          response match {
            case Some(_)  => Redirect(routes.CategoryController.list) //成功
            case None     => NotFound(views.html.error.page404()) //category(id)が存在しないため更新不可
          }
        }
      }
    )
  }

  /**
   * POST /todo/category/new
  */
  def store() = Action async {implicit req =>
    CategoryFormData.form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[CategoryFormData]) => {
        for {
          categorys <- categoryRepos.all
        } yield {
          BadRequest(views.html.todo.category.register(categorys.map(_.v), formWithErrors, ViewValueHome.vv_edit))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (categoryFormData: CategoryFormData) => {
        //DBに追加
        val category_new = Category.apply(categoryFormData.name, categoryFormData.slug, color=categoryFormData.color)
        for {
          id <- categoryRepos.add(category_new)
        } yield {
          Redirect(routes.CategoryController.list)
        }
      }
    )
  }

  /**
   * POST   /todo/category/delete
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
        val t = todoRepos.getByCategoryId(Category.Id(id))
        for {
          old <- categoryRepos.remove(Category.Id(id))
          todo_list <- t
          results <- old match {
            case None => Future.successful(Seq.empty)
            case Some(value) => {
              Future.sequence(todo_list.map( todo => 
                todoRepos.update(todo.map(_.copy(category=Category.Id(6)))) //category(6): noCateogry
              ))
            }
          }
        } yield {
          old match {
            case None => BadRequest("Invalid id")   //category(id)のremoveに失敗
            case Some(value) => {
              results.find(_ == None) match {
                case None => Redirect(routes.CategoryController.list) //成功
                case Some(_) => InternalServerError("Some todo may be uncorrect category") //todoのcategory更新に失敗
              }
            }
          }
        }
      }
      case None     => Future.successful(BadRequest("Invalid id")) //リクエストにidが含まれていない
    }
  }
}
