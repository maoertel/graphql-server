package io.github.maoertel.sangriaserver.persistence

import io.github.maoertel.sangriaserver.model.Product
import org.mongodb.scala.MongoCredential.createCredential
import org.mongodb.scala.connection.{ClusterSettings, SslSettings}
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection}

trait Database {
  def getCollection(coll: Collections): MongoCollection[Product]
}

sealed trait Collections {
  def name: String
}

case object Products extends Collections {
  def name = "products"
}

object Database {

  def apply(dbConfig: DbConfig): Database = new Database {

    private lazy val client: MongoClient = {
      val credential = createCredential(dbConfig.user, "admin", dbConfig.password)

      val mongoClientSettings = MongoClientSettings
        .builder()
        .applyToClusterSettings { (builder: ClusterSettings.Builder) =>
          builder.applyConnectionString(dbConfig.connectionString); ()
        }
        .applyToSslSettings { (builder: SslSettings.Builder) =>
          builder.enabled(true); ()
        }
        .credential(credential)
        .build()

      val mongoClient: MongoClient = MongoClient(mongoClientSettings)

      System.setProperty("org.mongodb.async.type", "netty")
      mongoClient
    }

    private lazy val db = client.getDatabase(dbConfig.dbName).withCodecRegistry(dbConfig.codecRegistry)

    override def getCollection(coll: Collections): MongoCollection[Product] = db.getCollection(coll.name)
  }
}
