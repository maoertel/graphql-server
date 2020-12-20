package io.github.maoertel.sangriaserver.graphql

import io.circe.generic.auto._
import io.github.maoertel.sangriaserver.GraphQlService
import io.github.maoertel.sangriaserver.model.{Picture, Product}
import org.mongodb.scala.bson.ObjectId
import sangria.macros.derive._
import sangria.marshalling.circe._
import sangria.schema._
import sangria.validation.ValueCoercionViolation

import scala.concurrent.ExecutionContext.global

object GraphQlSchema {

  implicit val ec = global

  case object ObjectIdViolation extends ValueCoercionViolation("String or Int value expected")

  implicit val ObjectIdType: ScalarAlias[ObjectId, String] = ScalarAlias[ObjectId, String](
    StringType,
    toScalar = _.toString,
    fromScalar = idString =>
      try Right(new ObjectId(idString))
      catch {
        case _: IllegalArgumentException => Left(ObjectIdViolation)
      })

  implicit val PictureType = ObjectType(
    "Picture",
    "The product picture",
    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType), description = Some("Picture CDN URL"), resolve = _.value.url)
    )
  )

  trait Identifiable {
    def id: ObjectId
  }

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](Field("id", ObjectIdType, resolve = _.value.id)))

  val ProductType =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture")
    )

  val Id = Argument("id", ObjectIdType)

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

  val ProductInputArgument = Argument("productDraft", ProductInputType)

  val MutationType = ObjectType(
    "Mutation",
    fields[GraphQlService, Unit](
      Field(
        "updateProduct",
        OptionType(ProductType),
        description = Some("Updates a product by ID"),
        arguments = Id :: ProductInputArgument :: Nil,
        resolve = c => c.ctx.products.updateProductById(c arg Id, c arg ProductInputArgument)
      ),
      Field(
        "createProduct",
        OptionType(ProductType),
        description = Some("Creates a product from ProductInput."),
        arguments = ProductInputArgument :: Nil,
        resolve = c => c.ctx.products.createProduct(c arg ProductInputArgument)
      ))
  )

  lazy val schema: Schema[GraphQlService, Unit] = Schema(QueryType, Some(MutationType))

}
