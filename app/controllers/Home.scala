/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._

import model.ViewValueHome

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
  * GET /
  */
  def index() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.Home(vv))
  }

  def notFound() = Action {implicit req =>
    NotFound(views.html.error.page404())
  }
}
