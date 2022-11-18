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
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import com.softwaremill.macwire

import model.ViewValueHome
import lib.model.Category
import json.writes.JsValueCategory
import json.reads.JsValueCreateCategory
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
    } yield {
      val jsValue = categorys.map(JsValueCategory.apply(_))
      Ok(Json.toJson(jsValue))
    }
  }

  /**
    * GET /api/todo/category/:id
    *
    * @param id
    */
  def get(id: Long) = Action async { implicit req =>
    for{
      category_opt <- categoryRepos.get(Category.Id(id))
    } yield {
      category_opt match {
        case None => NotFound("Invalid id")
        case Some(category) => {
          val jsValue = JsValueCategory.apply(category)
          Ok(Json.toJson(jsValue))
        }
      }
    }
  }

  /**
    * POST /todo/category/:id/update
    */
  def update(id: Long) = Action(parse.json) async { implicit request =>
    request.body.validate[JsValueCreateCategory].fold(
      errors => {
        //TODO: どうするのが良いか考える
        Future.successful(BadRequest("Request data is unacceptable"))
      },
      categoryData => {
        //DBのデータをupdate
        for {
          category_old <- categoryRepos.get(Category.Id(id))
          response <- category_old match {
            case None => Future.successful(None)
            case Some(category) => categoryRepos.update(category.map(_.copy(  name=categoryData.name, 
                                                                              slug=categoryData.slug,
                                                                              color=Category.ColorStatus.apply(categoryData.color))))
          }
        } yield {
          response match {
            case None     => BadRequest("Requested category is not exist") //category(id)が存在しないため更新不可
            case Some(_)  => Ok("updated successfully") //成功
          }
        }
      }
    )
  }

  /**
   * POST /todo/category/new
  */
  def store() = Action(parse.json) async {implicit req =>
    req.body.validate[JsValueCreateCategory].fold(
      //パースと変換がうまくいかなかった時
      error => {
        //TODO: どうするのが良いか考える
        println(req.body.toString())
        Future.successful(BadRequest("Request data is unacceptable"))
      },
      categoryData => {
        //DBに追加
        val category_new = Category.apply(categoryData.name, categoryData.slug, color=Category.ColorStatus.apply(categoryData.color))
        for {
          id <- categoryRepos.add(category_new)
        } yield {
          Ok("Stored successfully")
        }
      }
    )
  }

  /**
   * POST   /todo/category/delete
   */
  def delete(id: Long) = Action async { implicit request: Request[AnyContent] =>
    id match {
      case Category.noCategory_id => Future.successful(BadRequest("Invalid id")) //noCategoryは削除不可
      case id => {
        for {
          old_opt <- categoryRepos.remove(Category.Id(id))
          noCategory_opt <- categoryRepos.get(Category.noCategory_id)

          _ <- old_opt match {
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
                  Ok("deleted successfully") //成功
                }
              }
            }
          }
        }
      }
    }
  }
}
