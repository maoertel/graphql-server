package io.github.maoertel.sangriaserver.repo

import io.github.maoertel.sangriaserver.model.{Product, ProductInput}
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

  def deleteProduct(id: ObjectId): Future[Option[ObjectId]]
}

object ProductRepository {

  def apply(
    productsColl: MongoCollection[Product]
  )(implicit ec: ExecutionContextExecutor): ProductRepository = new ProductRepository {

    import cats.syntax.option._

    def getProduct(id: ObjectId): Future[Option[Product]] =
      productsColl.find[Product](equal("_id", id)).toFuture().map(_.headOption)

    def getProducts: Future[List[Product]] =
      productsColl.find[Product]().toFuture().map(_.toList)

    def createProduct(productDraft: ProductInput): Future[Option[Product]] = {
      val product = Product(id = new ObjectId(), productDraft.name, productDraft.description)
      productsColl
        .insertOne(product)
        .toFuture()
        .map(r => if (r.wasAcknowledged()) product.some else Option.empty)
    }

    def updateProductById(id: ObjectId, productDraft: ProductInput): Future[Option[Product]] =
      productsColl
        // replace with findOneAndUpdate to prevent form two roundtrips
        .updateOne(
          equal("_id", id),
          Updates.combine(set("name", productDraft.name), set("description", productDraft.description))
        )
        .toFuture()
        .flatMap(_ => getProduct(id))

    def deleteProduct(id: ObjectId): Future[Option[ObjectId]] =
      productsColl
        .deleteOne(equal("_id", id))
        .toFuture()
        .map(r => if (r.wasAcknowledged()) id.some else Option.empty)

  }
}
