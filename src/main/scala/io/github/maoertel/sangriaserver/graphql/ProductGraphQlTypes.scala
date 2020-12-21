package io.github.maoertel.sangriaserver.graphql

import io.circe.generic.auto._
import io.github.maoertel.sangriaserver.GraphQlService
import io.github.maoertel.sangriaserver.graphql.CommonGraphQlTypes._
import io.github.maoertel.sangriaserver.model.{Picture, Product, ProductInput}
import sangria.macros.derive._
import sangria.marshalling.circe._
import sangria.schema.{Argument, Field, InputObjectType, IntType, ListType, ObjectType, OptionType, StringType, fields}

object ProductGraphQlTypes extends GraphQlTypes {

  implicit val PictureType: ObjectType[Unit, Picture] = ObjectType(
    "Picture",
    "The product picture",
    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType), description = Some("Picture CDN URL"), resolve = _.value.url)
    )
  )

  implicit val PictureInputType: InputObjectType[Picture] = deriveInputObjectType[Picture](
    InputObjectTypeName("PictureInput")
  )

  val ProductType: ObjectType[Unit, Product] =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture")
    )

  implicit val ProductInputType: InputObjectType[ProductInput] = deriveInputObjectType[ProductInput](
    InputObjectTypeName("ProductInput")
  )

  val ProductInputArgument: Argument[ProductInput] = Argument("productDraft", ProductInputType)

  override val queryFields: List[Field[GraphQlService, Unit]] = fields[GraphQlService, Unit](
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

  override val mutationFields: List[Field[GraphQlService, Unit]] = fields[GraphQlService, Unit](
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
    )
  )
}
