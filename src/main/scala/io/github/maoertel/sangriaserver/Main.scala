package io.github.maoertel.sangriaserver

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import io.github.maoertel.sangriaserver.graphql.GraphQlSchema
import io.github.maoertel.sangriaserver.graphql.GraphQlSchema.Product.personCodecProvider
import io.github.maoertel.sangriaserver.persistence.{Database, DbConfig}
import io.github.maoertel.sangriaserver.repo.ProductRepository
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.ConnectionString
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import sangria.schema.Schema

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends IOApp {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- IO(ConfigFactory.load())

      port = config.getInt("serverConfig.port")
      host = config.getString("serverConfig.host")
      server = ServerConfig(host, port)

      dbConfig <- IO(ConfigFactory.load("db.conf"))
      connectionString = dbConfig.getString("db.connectionString")
      dbName = dbConfig.getString("db.name")
      user = dbConfig.getString("db.user")
      password = dbConfig.getString("db.password")
      codecRegistry = fromRegistries(fromProviders(personCodecProvider), DEFAULT_CODEC_REGISTRY)

      dataBaseConfig = DbConfig(ConnectionString(connectionString), dbName, user, password.toCharArray, codecRegistry)

      database = Database(dataBaseConfig)

      schema = GraphQlSchema.schema

      productRepo = ProductRepository(database.getCollection("products"))
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
