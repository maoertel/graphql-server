package io.github.maoertel.sangriaserver.repo

import io.github.maoertel.sangriaserver.graphql.GraphQlSchema.{Product, ProductInput}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates
import org.mongodb.scala.model.Updates.set

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProductRepository {
  def getProduct(id: String): Future[Option[Product]]

  def getProducts: Future[List[Product]]

  def updateProductsById(id: String, productDraft: ProductInput): Future[Option[Product]]
}

object ProductRepository {

  def apply(
    productsColl: MongoCollection[_]
  )(implicit ec: ExecutionContextExecutor): ProductRepository = new ProductRepository {

    def getProduct(id: String): Future[Option[Product]] =
      productsColl.find[Product](equal("id", id)).toFuture().map(_.headOption)

    def getProducts: Future[List[Product]] =
      productsColl.find[Product]().toFuture().map(_.toList)

    def updateProductsById(id: String, productDraft: ProductInput): Future[Option[Product]] =
      productsColl
        .updateOne(
          equal("id", id),
          Updates.combine(set("name", productDraft.name), set("description", productDraft.description))
        )
        .toFuture()
        .flatMap(_ => getProduct(id))
  }
}
