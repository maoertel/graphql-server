package io.github.maoertel.sangriaserver.repo

import io.github.maoertel.sangriaserver.graphql.GraphQlSchema.{Product, ProductInput}

trait ProductRepository {
  def product(id: String): Option[Product]

  def products: List[Product]

  def updateProductsById(id: String, productDraft: ProductInput): Option[Product]
}

object ProductRepository {

  def apply(): ProductRepository = new ProductRepository {
    private val Products = List(
      Product("1", "Cheesecake", "Tasty"),
      Product("2", "Health Potion", "+50 HP"))

    def product(id: String): Option[Product] =
      Products find (_.id == id)

    def products: List[Product] = Products

    def updateProductsById(id: String, productDraft: ProductInput): Option[Product] =
      Some(Product(id = id, name = productDraft.name, description = productDraft.description))
  }
}
