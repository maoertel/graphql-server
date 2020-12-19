package io.github.maoertel.sangriaserver.graphql

import cats.effect.IO
import io.github.maoertel.sangriaserver.graphql.model.GqlGetInputDraft
import io.github.maoertel.sangriaserver.validation.InputValidator.{validateGetInput, validatePostInput}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}

object GraphQlServerRoutes {

  object QueryMatcher extends QueryParamDecoderMatcher[String]("query")
  object OpMatcher extends OptionalQueryParamDecoderMatcher[String]("operationName")
  object VarMatcher extends OptionalQueryParamDecoderMatcher[String]("variables")

  def graphQlRoutes(gql: GraphQlExecutor): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      case GET -> Root / "graphql" :? QueryMatcher(query) +& OpMatcher(operationName) +& VarMatcher(variables) =>
        validateGetInput _ andThen gql.process apply GqlGetInputDraft(query, operationName, variables) flatMap(Ok(_))

      case request @ POST -> Root / "graphql" =>
        validatePostInput _ andThen gql.process apply request flatMap (Ok(_))
    }
  }
}
