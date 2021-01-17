package io.github.maoertel.sangriaserver

import cats.data.Reader
import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import io.github.maoertel.sangriaserver.graphql.{GraphQlSchema, ProductGraphQlTypes}
import io.github.maoertel.sangriaserver.model.Product.productCodecProvider
import io.github.maoertel.sangriaserver.persistence.{Database, DbConfig, Products}
import io.github.maoertel.sangriaserver.repo.ProductRepository
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.ConnectionString
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import sangria.schema.Schema

import scala.concurrent.ExecutionContextExecutor

trait Configuration {
  def getEnvironment: IO[Environment]
}

object Configuration {

  def apply(
    application: String,
    db: String
  )(implicit ec: ExecutionContextExecutor): Configuration = new Configuration {

    private val serverConfigReader: Reader[Config, ServerConfig] = Reader { config =>
      val port = config.getInt("serverConfig.port")
      val host = config.getString("serverConfig.host")
      ServerConfig(host, port)
    }

    private val dbConfigReader: Reader[Config, Database] = Reader { dbConfig =>
      val connString = dbConfig.getString("db.connectionString")
      val dbName = dbConfig.getString("db.name")
      val user = dbConfig.getString("db.user")
      val password = dbConfig.getString("db.password")
      val codecRegistry = fromRegistries(fromProviders(productCodecProvider), DEFAULT_CODEC_REGISTRY)
      val dataBaseConfig = DbConfig(ConnectionString(connString), dbName, user, password.toCharArray, codecRegistry)
      Database(dataBaseConfig)
    }

    private val productRepoReader: Reader[Config, ProductRepository] =
      dbConfigReader andThen Reader[Database, ProductRepository](db => ProductRepository(db.getCollection(Products)))

    private val graphQlServiceReader: Reader[Config, GraphQlService] =
      productRepoReader andThen Reader[ProductRepository, GraphQlService](GraphQlService)

    private val environmentReader: Reader[Config, Environment] = for {
      server <- serverConfigReader
      graphQlSchema = GraphQlSchema(ProductGraphQlTypes).schema
      graphQlContext <- graphQlServiceReader
    } yield Environment(server, graphQlSchema, graphQlContext)

    private val config: Config = {
      val applicationConf = ConfigFactory.load(application)
      val dbConf = ConfigFactory.load(db)
      applicationConf.withFallback(dbConf)
    }

    override def getEnvironment: IO[Environment] = IO(environmentReader.run(config))
  }
}

case class ServerConfig(host: String, port: Int)

case class GraphQlService(products: ProductRepository)

case class Environment(
  server: ServerConfig,
  schema: Schema[GraphQlService, Unit],
  graphQlService: GraphQlService
)
