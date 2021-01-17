package io.github.maoertel.sangriaserver.graphql

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Json}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class GqlGetInputDraft(
  query: String,
  operationName: Option[String],
  variables: Option[String]
)

case class GqlPostInputDraft(
  query: String,
  operationName: Option[String],
  variables: Option[Json]
)
object GqlPostInputDraft {
  implicit val inputDraftDecoder: EntityDecoder[IO, GqlPostInputDraft] = jsonOf[IO, GqlPostInputDraft]
}

case class GraphQlErrorResponse(errors: List[GraphQlError])

case class GraphQlError(
  message: String,
  locations: Option[List[(String, Int)]] = None,
  paths: Option[List[String]] = None,
  code: Option[String] = None,
  timestamp: Option[String] = None
)
object GraphQlError {
  implicit val entityEncoder: Encoder[GraphQlError] = deriveEncoder[GraphQlError].mapJson(_.dropNullValues)
}
