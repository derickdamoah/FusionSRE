package controllers

import connectors.DynamoDBConnector
import forms.MovieForm.movieForm
import play.api.i18n.I18nSupport
import play.api.mvc._
import views.html.searchResults

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MovieController @Inject()(
  val controllerComponents: ControllerComponents,
  searchResultsView: searchResults, dynamoDBConnector: DynamoDBConnector
)
  extends BaseController with I18nSupport{

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def searchMovies(
                    title: Option[String],
                    year: Option[Int],
                    cast: Option[String],
                    genres: Option[String]
                  ): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    implicit val ec: ExecutionContext = controllerComponents.executionContext

    println(Console.RED + "Year: " + year + Console.RESET)

    dynamoDBConnector.searchMovies(title, year, cast, genres).map {movies =>
      Ok(searchResultsView(movieForm, movies))
    }

  }
}