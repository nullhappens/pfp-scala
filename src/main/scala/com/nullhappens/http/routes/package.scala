package com.nullhappens.http

import java.{ util => ju }

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{Uuid, ValidBigDecimal}
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import squants.market.{Money, USD}

import com.nullhappens.http.routes.{ItemDescriptionParam, ItemIdParam, ItemNameParam, PriceParam}
import com.nullhappens.models._

package object routes {
  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
  }
  @newtype case class CategoryParam(value: NonEmptyString) {
    def toDomain: CategoryName =
      CategoryName(value.value.toLowerCase.capitalize)
  }
  @newtype case class ItemNameParam(value: NonEmptyString) {
    def toDomain: ItemName = ItemName(value.value.toLowerCase.capitalize)
  }
  @newtype case class ItemDescriptionParam(value: NonEmptyString) {
    def toDomain: ItemDescription =
      ItemDescription(value.value.toLowerCase.capitalize)
  }
  @newtype case class PriceParam(value: String Refined ValidBigDecimal) {
    def toDomain: Money = USD(BigDecimal(value.value))
  }
  @newtype case class ItemIdParam(value: String Refined Uuid)
}

final case class CreateItemParam(
    name: ItemNameParam,
    description: ItemDescriptionParam,
    price: PriceParam) {
  def toDomain: CreateItem = CreateItem(
    name.toDomain,
    description.toDomain,
    price.toDomain,
    BrandId(ju.UUID.randomUUID()),
    CategoryId(ju.UUID.randomUUID())
  )
}

final case class UpdateItemParam(id: ItemIdParam, price: PriceParam) {
  def toDomain: UpdateItem =
    UpdateItem(ItemId(ju.UUID.fromString(id.value.value)), price.toDomain)
}
