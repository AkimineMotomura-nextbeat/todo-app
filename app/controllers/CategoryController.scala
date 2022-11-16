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
      category_opt <- categoryRepos.get(Category.Id(id))
    }yield {
      
      category_opt match {
        case Some(category) => {
          val filledForm = CategoryFormData.apply(category)
          Ok(views.html.todo.category.editor(category.id, filledForm, ViewValueHome.vv_edit))
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
            BadRequest(views.html.todo.category.editor(id, formWithErrors, ViewValueHome.vv_edit))
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
      case Some(Category.noCategory_id) => Future.successful(BadRequest("Invalid id")) //noCategoryは削除不可
      case Some(id) => {
        for {
          old_opt <- categoryRepos.remove(Category.Id(id))
          noCategory_opt <- categoryRepos.get(Category.noCategory_id)

          results <- old_opt match {
            case None         => Future.successful(Seq.empty)
            case Some(old)  => { 
              noCategory_opt match {
                case None => Future.successful(Seq.empty)
                case Some(noCategory) => todoRepos.updateCategory(old, noCategory)
              }
            }
          }
        } yield {
          old_opt match {
            case None => BadRequest("Invalid id")   //category(id)のremoveに失敗
            case Some(old) => {
              noCategory_opt match {
                case None => InternalServerError("Some todo may be uncorrect category") //何故かnoCategoryが削除されていた場合
                case Some(noCategory) => {
                  results.isEmpty match {
                    case true   => Redirect(routes.CategoryController.list) //成功
                    case false  => Redirect(routes.CategoryController.list) //成功
                  }
                  //resultsを評価したいがFuture.onFailure以外は全て成功
                }
              }
            }
          }
        }
      }
      case None     => Future.successful(BadRequest("Invalid id")) //リクエストにidが含まれていない
    }
  }
}
