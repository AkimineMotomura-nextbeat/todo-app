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
            case Some(_)  => Redirect(routes.CategoryController.list)
            case None     => NotFound(views.html.error.page404())
          }
        }

        /*
        展開したいもののイメージ

        categoryRepos.get(Category.Id(id)) flatMap { category_old => 
          category_old match {
            case None => Future.successful(NotFound(views.html.error.page404()))
            case Some(category) => {
              categoryRepos.update(category_old.get.map(_.copy( name=categoryFormData.name, 
                                                                slug=categoryFormData.slug,
                                                                color=categoryFormData.color))) flatMap { response =>
                response match {
                  case Some(_)  => Future.successful(Redirect(routes.CategoryController.list))
                  case None     => Future.successful(NotFound(views.html.error.page404()))
                }
              }
            }
          }
        }
        */
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
        val t = todoRepos.all
        for {
          todo_list <- t
          old <- categoryRepos.remove(Category.Id(id))
          results <- Future.sequence(old match {
            case None => Seq.empty
            case Some(_) => {
              todo_list.filter(_.v.category == id).map( todo => 
                todoRepos.update(todo.map(_.copy(category=Category.Id(6)))) //category(6): noCateogry
              )
            }
          })
        } yield {
          results.find(_ == None) match {
            case None => Redirect(routes.CategoryController.list)
            case Some(_) => Redirect(routes.CategoryController.list) //500errorに置き換え
          }
        }
        
        //CategoryControllerでtodoTable操作してしまってるのが良く無いかも
      }
      case None     => Future.successful(BadRequest("Invalid id"))
    }
  }
}
