package com.nullhappens

import io.estatico.newtype.macros.newtype
import java.{ util => ju }

package object models {
  @newtype case class BrandId(value: ju.UUID)
  @newtype case class BrandName(value: String)
  @newtype case class CategoryId(value: ju.UUID)
  @newtype case class CategoryName(value: String)
  @newtype case class ItemId(value: ju.UUID)
  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)
  @newtype case class Quantity(value: Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])
  @newtype case class CartId(value: ju.UUID)
  @newtype case class UserId(value: ju.UUID)
  @newtype case class OrderId(uuid: ju.UUID)
  @newtype case class PaymentId(uuid: ju.UUID)
  @newtype case class Password(value: String)
  @newtype case class UserName(value: String)
  @newtype case class JwtToken(value: String)
  @newtype case class CardHolderName(value: String)
  @newtype case class CardNumber(value: Long)
  @newtype case class CardExpiration(value: Int)
  @newtype case class CVV(value: Int)
}
