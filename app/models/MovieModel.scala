package models

case class MovieModel(
                       title: String,
                       year: Int,
                       cast: Seq[String],
                       genres: Seq[String],
                       href: String,
                       extract: String,
                       thumbnail: String,
                       thumbnail_width: Int,
                       thumbnail_height: Int
                     )
