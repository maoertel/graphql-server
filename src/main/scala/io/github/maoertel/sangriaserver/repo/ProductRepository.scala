package io.github.maoertel.sangriaserver.repo

import io.github.maoertel.sangriaserver.graphql.GraphQlSchema.ProductInput
import io.github.maoertel.sangriaserver.model.Product
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates
import org.mongodb.scala.model.Updates.set

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProductRepository {
  def getProduct(id: ObjectId): Future[Option[Product]]

  def getProducts: Future[List[Product]]

  def createProduct(productDraft: ProductInput): Future[Option[Product]]

  def updateProductById(id: ObjectId, productDraft: ProductInput): Future[Option[Product]]
}

object ProductRepository {

  def apply(
    productsColl: MongoCollection[Product]
  )(implicit ec: ExecutionContextExecutor): ProductRepository = new ProductRepository {

    def getProduct(id: ObjectId): Future[Option[Product]] =
      productsColl.find[Product](equal("_id", id)).toFuture().map(_.headOption)

    def getProducts: Future[List[Product]] =
      productsColl.find[Product]().toFuture().map(_.toList)

    def createProduct(productDraft: ProductInput): Future[Option[Product]] =
      productsColl
        .insertOne(Product(id = new ObjectId(), productDraft.name, productDraft.description))
        .toFuture()
        .map(_.getInsertedId.asObjectId().getValue)
        .flatMap(getProduct)

    def updateProductById(id: ObjectId, productDraft: ProductInput): Future[Option[Product]] =
      productsColl
        // replace with findOneAndUpdate
        .updateOne(
          equal("_id", id),
          Updates.combine(set("name", productDraft.name), set("description", productDraft.description))
        )
        .toFuture()
        .flatMap(_ => getProduct(id))
  }
}
