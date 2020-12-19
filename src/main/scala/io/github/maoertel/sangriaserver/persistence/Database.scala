package io.github.maoertel.sangriaserver.persistence

import io.github.maoertel.sangriaserver.graphql.GraphQlSchema.Product
import org.mongodb.scala.{MongoClient, MongoCollection}

trait Database {
  def getCollection(name: String): MongoCollection[Product]
}

object Database {

  def apply(connectionString: String, dbName: String): Database = new Database {

    private lazy val client: MongoClient = {
      System.setProperty("org.mongodb.async.type", "netty")
      MongoClient(connectionString)
    }

    private lazy val db = client.getDatabase(dbName)

    override def getCollection(name: String): MongoCollection[Product] = db.getCollection(name)
  }
}
