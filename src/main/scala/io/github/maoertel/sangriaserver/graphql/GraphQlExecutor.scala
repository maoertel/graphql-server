package io.github.maoertel.sangriaserver.graphql

import cats.data.ValidatedNel
import cats.effect.{ContextShift, IO}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import io.github.maoertel.sangriaserver.GraphQlService
import io.github.maoertel.sangriaserver.graphql.model.{GraphQlError, GraphQlErrorResponse}
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.circe._
import sangria.schema._

import scala.concurrent.ExecutionContextExecutor

trait GraphQlExecutor {
  def process(validatedDraft: IO[ValidatedNel[Throwable, (Document, Option[String], Json)]]): IO[Json]
}

object GraphQlExecutor {

  def impl(
    schema: Schema[GraphQlService, Unit],
    graphQlService: GraphQlService
  )(implicit
    cs: ContextShift[IO],
    ec: ExecutionContextExecutor
  ): GraphQlExecutor = (validatedDraft: IO[ValidatedNel[Throwable, (Document, Option[String], Json)]]) =>
    validatedDraft.flatMap(
      _.fold(
        nel => IO(GraphQlErrorResponse(nel.map(e => GraphQlError(e.getMessage)).toList).asJson),
        { case (query, operationName, variables) => execute(query, operationName, variables, schema, graphQlService) }
      ))

  private def execute(
    query: Document,
    operationName: Option[String],
    variables: Json,
    schema: Schema[GraphQlService, Unit],
    graphQlService: GraphQlService
  )(implicit
    cs: ContextShift[IO],
    ec: ExecutionContextExecutor
  ): IO[Json] =
    IO.fromFuture {
      IO(
        Executor.execute(
          schema = schema,
          queryAst = query,
          userContext = graphQlService,
          operationName = operationName,
          variables = variables
        ))
    }
}
