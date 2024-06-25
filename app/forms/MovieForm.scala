package forms

import models.MovieModel
import play.api.data.Form
import play.api.data.Forms._

object MovieForm {

  val movieForm: Form[MovieModel] = Form(
    mapping(
      "title" -> text,
      "year" -> number,
      "cast" -> seq(text),
      "genres" -> seq(text),
      "href" -> text,
      "extract" -> text,
      "thumbnail" -> text,
      "thumbnail_width" -> number,
      "thumbnail_height" -> number
    )(MovieModel.apply)(MovieModel.unapply)
  )
}

