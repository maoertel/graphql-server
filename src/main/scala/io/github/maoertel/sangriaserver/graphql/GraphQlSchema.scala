package io.github.maoertel.sangriaserver.graphql

import io.github.maoertel.sangriaserver.GraphQlService
import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.codecs.Macros

import scala.concurrent.ExecutionContext.global

object GraphQlSchema {

  implicit val ec = global

  case class Picture(width: Int, height: Int, url: Option[String])

  import sangria.schema._

  // creating a picture GraphQL Object Type
  implicit val PictureType = ObjectType(
    "Picture",
    "The product picture",
    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType), description = Some("Picture CDN URL"), resolve = _.value.url)
    )
  )

  import sangria.macros.derive._

  trait Identifiable {
    def id: String
  }

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](Field("id", StringType, resolve = _.value.id)))

  // create a GraphQL ObjectType with Interface with deriving
  case class Product(
    id: String,
    name: String,
    description: String
  ) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
  }
  object Product {
    val personCodecProvider: CodecProvider = Macros.createCodecProviderIgnoreNone[Product]()
  }

  val ProductType =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture")
    )

  val Id = Argument("id", StringType)

  val QueryType = ObjectType(
    "Query",
    fields[GraphQlService, Unit](
      Field(
        "product",
        OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Id :: Nil,
        resolve = c => c.ctx.products.getProduct(c arg Id)
      ),
      Field(
        "products",
        ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.products.getProducts)
    )
  )

  case class ProductInput(name: String, description: String)

  implicit val PictureInputType = deriveInputObjectType[Picture](
    InputObjectTypeName("PictureInput")
  )

  implicit val ProductInputType = deriveInputObjectType[ProductInput](
    InputObjectTypeName("ProductInput")
  )

  import io.circe.generic.auto._
  import sangria.marshalling.circe._

  val ProductInputArgument = Argument("productDraft", ProductInputType)

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphQlService, Unit](
      Field(
        "updateProduct",
        OptionType(ProductType),
        description = Some("Updates a product by ID"),
        arguments = Id :: ProductInputArgument :: Nil,
        resolve = c => c.ctx.products.updateProductsById(c arg Id, c arg ProductInputArgument)
      ))
  )

  lazy val schema: Schema[GraphQlService, Unit] = Schema(QueryType, Some(MutationType))

}
