package connectors

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ScanRequest}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import models.MovieModel
import play.api.mvc.ControllerComponents

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

class DynamoDBConnector @Inject() (val controllerComponents: ControllerComponents) {
  implicit val ec: ExecutionContext = controllerComponents.executionContext

  private val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build()
  private val dynamoDB = new DynamoDB(client)
  private val tableName = "fusion-sre-table"

  def searchMovies(title: Option[String], year: Option[Int], cast: Option[String], genres: Option[String]): Future[Seq[MovieModel]] = Future {
    val filterExpressions = Seq(
      title.filter(_.nonEmpty).map(_ => "contains(#title, :titleVal)"),
      year.map(_ => "#year = :yearVal"),
      cast.filter(_.nonEmpty).map(_ => "contains(#cast, :castVal)"),
      genres.filter(_.nonEmpty).map(_ => "contains(#genres, :genresVal)")
    ).flatten

    val filters = filterExpressions match {
      case Seq() => ""
      case nonEmptyFilters => nonEmptyFilters.mkString(" AND ")
    }


    val buildExpressionAttributeNames = collection.mutable.Map[String, String]()
    val buildExpressionAttributeValues = collection.mutable.Map[String, AttributeValue]()

    if (title.isDefined && title.get != "") {
      buildExpressionAttributeNames += ("#title" -> "title")
      buildExpressionAttributeValues += (":titleVal" -> new AttributeValue().withS(title.get))
    }

    if (year.isDefined) {
      buildExpressionAttributeNames += ("#year" -> "year")
      buildExpressionAttributeValues += (":yearVal" -> new AttributeValue().withN(year.get.toString))
    }

    if (cast.isDefined && cast.get != "") {
      buildExpressionAttributeNames += ("#cast" -> "cast")
      buildExpressionAttributeValues += (":castVal" -> new AttributeValue().withS(cast.get))
    }

    if (genres.isDefined && genres.get != "") {
      buildExpressionAttributeNames += ("#genres" -> "genres")
      buildExpressionAttributeValues += (":genresVal" -> new AttributeValue().withS(genres.get))
    }

    val expressionAttributeNames = buildExpressionAttributeNames.asJava
    val expressionAttributeValues = buildExpressionAttributeValues.asJava

    val scanRequest = new ScanRequest()
      .withTableName(tableName)

    if (filters.nonEmpty) {
      scanRequest
        .withTableName(tableName)
        .withFilterExpression(filters)
        .withExpressionAttributeNames(expressionAttributeNames)
        .withExpressionAttributeValues(expressionAttributeValues)
    }

    val regex: Regex = """\{S:\s*([^,]+),\}""".r

    val result = client.scan(scanRequest)

    result.getItems.asScala.map { item =>
      val cast = item.get("cast").getL.toArray.map(_.toString).toSeq.flatMap {
        case regex(value) => Some(value)
        case _ => None
      }

      val genres = item.get("genres").getL.toArray.map(_.toString).toSeq.flatMap {
        case regex(value) => Some(value)
        case _ => None
      }

      MovieModel(
        Option(item.get("title")).map(_.getS).getOrElse(""),
        Option(item.get("year")).map(_.getN.toInt).getOrElse(0),
        cast,
        genres,
        Option(item.get("href")).map(_.getS).getOrElse(""),
        Option(item.get("extract")).map(_.getS).getOrElse(""),
        Option(item.get("thumbnail")).map(_.getS).getOrElse(""),
        Option(item.get("thumbnail_width")).map(_.getN.toInt).getOrElse(0),
        Option(item.get("thumbnail_height")).map(_.getN.toInt).getOrElse(0)
      )
    }.toSeq
  }
}
