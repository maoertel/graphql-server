package io.github.maoertel.sangriaserver

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import io.github.maoertel.sangriaserver.graphql.GraphQlSchema
import io.github.maoertel.sangriaserver.repo.ProductRepository
import sangria.schema.Schema

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends IOApp {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- IO(ConfigFactory.load())
      port = config.getInt("serverConfig.port")
      host = config.getString("serverConfig.host")
      server = ServerConfig(host, port)

      schema = GraphQlSchema.schema

      productRepo = ProductRepository()
      graphQlContext = GraphQlService(productRepo)

      env = Environment(server, schema, graphQlContext)

      server <- Server.stream(env).compile.drain.as(ExitCode.Success)
    } yield server
}

case class ServerConfig(host: String, port: Int)

case class GraphQlService(products: ProductRepository)

case class Environment(
  server: ServerConfig,
  schema: Schema[GraphQlService, Unit],
  graphQlService: GraphQlService
)
