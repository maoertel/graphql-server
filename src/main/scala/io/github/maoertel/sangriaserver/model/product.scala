package io.github.maoertel.sangriaserver.model

import io.github.maoertel.sangriaserver.graphql.CommonGraphQlTypes.Identifiable
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.pojo.annotations.BsonId
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.annotations.BsonProperty
import org.mongodb.scala.bson.codecs.Macros

case class Product(
                    @BsonId
                    @BsonProperty("_id")
                    id: ObjectId,
                    name: String,
                    description: String
                  ) extends Identifiable {
  def picture(size: Int): Picture =
    Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
}

object Product {
  val personCodecProvider: CodecProvider = Macros.createCodecProviderIgnoreNone[Product]()
}

case class ProductInput(name: String, description: String)

case class Picture(width: Int, height: Int, url: Option[String])
