package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(name: Option[String] = Option("Default Value Kishore")) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(name.getOrElse("Default Else Value Kishore")))
  }

//  def explore() = Action { implicit request: Request[AnyContent] =>
//    Ok(views.html.explore())
//  }
//
//  def tutorial() = Action { implicit request: Request[AnyContent] =>
//    Ok(views.html.tutorial())
//  }

}
