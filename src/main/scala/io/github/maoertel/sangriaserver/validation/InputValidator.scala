package io.github.maoertel.sangriaserver.validation

import cats.effect.IO
import io.circe.{Json, ParsingFailure}
import io.github.maoertel.sangriaserver.graphql.{GqlGetInputDraft, GqlPostInputDraft}
import org.http4s.Request
import sangria.ast.Document
import sangria.parser.QueryParser

object InputValidator {

  import cats.data.Validated._
  import cats.data._
  import cats.implicits._
  import io.circe.parser._

  def validateGetInput(draft: GqlGetInputDraft): IO[ValidatedNel[Throwable, (Document, Option[String], Json)]] = {
    def validateVariables(variables: Option[String]): ValidatedNel[ParsingFailure, Json] =
      variables.fold[ValidatedNel[ParsingFailure, Json]](Json.obj().valid)(parse(_).toValidatedNel)

    (validateQuery(draft.query), draft.operationName.validNel, validateVariables(draft.variables)).tupled.pure[IO]
  }

  def validatePostInput(request: Request[IO]): IO[ValidatedNel[Throwable, (Document, Option[String], Json)]] = {

    def invalidJsonOfType(name: String): Any => ValidatedNel[Throwable, Json] = { _: Any =>
      new Throwable(s"JSON $name not allowed in position of variables").invalidNel
    }

    request
      .attemptAs[GqlPostInputDraft]
      .toValidatedNel
      .map(validNel =>
        validNel
          .leftMap(nel => NonEmptyList.one(new Throwable(nel.head.message)))
          .andThen { draft =>
            (
              validateQuery(draft.query),
              draft.operationName.validNel,
              draft.variables.fold(Json.obj().validNel[Throwable]) { vars =>
                vars.fold[ValidatedNel[Throwable, Json]](
                  jsonNull = Json.obj().validNel,
                  jsonBoolean = invalidJsonOfType("boolean"),
                  jsonNumber = invalidJsonOfType("number"),
                  jsonString = parse(_).toValidatedNel,
                  jsonArray = invalidJsonOfType("array"),
                  jsonObject = _ => vars.validNel[Throwable]
                )
              }).tupled
          })
  }

  private def validateQuery(query: String): ValidatedNel[Throwable, Document] =
    QueryParser.parse(query).toEither.toValidatedNel
}
