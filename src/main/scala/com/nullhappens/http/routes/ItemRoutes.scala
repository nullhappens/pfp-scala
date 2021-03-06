package com.nullhappens.http.routes

import cats.{ Defer, Monad }
import eu.timepit.refined.auto._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import com.nullhappens.http.json._
import com.nullhappens.http.params._
import com.nullhappens.models._

final class ItemRoutes[F[_]: Defer: Monad](items: Items[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath: String = "/items"

  object BrandQueryParam
    extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(items.findAll)(b => items.findBy(b.toDomain)))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}

final case class CreateItemParam(
    name: ItemNameParam,
    description: ItemDescriptionParam,
    price: PriceParam,
    brandId: BrandId,
    categoryId: CategoryId) {
  def toDomain: CreateItem =
    CreateItem(
      name.toDomain,
      description.toDomain,
      price.toDomain,
      brandId,
      categoryId
    )
}
