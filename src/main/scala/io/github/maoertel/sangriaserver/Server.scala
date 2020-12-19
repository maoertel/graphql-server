package io.github.maoertel.sangriaserver

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream
import io.github.maoertel.sangriaserver.graphql.{GraphQlExecutor, GraphQlServerRoutes}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContextExecutor

object Server {

  def stream(env: Environment)(implicit T: Timer[IO], ec: ExecutionContextExecutor): Stream[IO, Nothing] = {

    implicit val cs: ContextShift[IO] = IO.contextShift(ec)

    val graphQlExecutor = GraphQlExecutor.impl(env.schema, env.graphQlService)
    val httpApp = GraphQlServerRoutes.graphQlRoutes(graphQlExecutor).orNotFound

    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[IO](ec)
      .bindHttp(env.server.port, env.server.host)
      .withHttpApp(finalHttpApp)
      .serve
      .map(exitCode => exitCode)
  }.drain
}
