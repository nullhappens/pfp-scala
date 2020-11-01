package com.nullhappens.models

import scala.util.control.NoStackTrace

import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import squants.market.Money

import com.nullhappens.effect.GenUUID
import com.nullhappens.effect.MonadThrow
import com.nullhappens.effect.ApThrow
import squants.market.USD
import cats.effect.Sync

trait ShoppingCart[F[_]] {
  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def delete(userId: UserId): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
}

final case object EmptyCartError extends NoStackTrace
final case class CartNotFound(userId: UserId) extends NoStackTrace

case class CartItem(item: Item, quantity: Quantity)
case class CartTotal(items: List[CartItem], total: Money)

object LiveShoppingCart {
  def make[F[_]: Sync](
      items: Items[F],
      redis: RedisCommands[F, String, String],
      exp: ShoppingCartExpiration
    ): F[ShoppingCart[F]] =
    Sync[F].delay(new LiveShoppingCart(items, redis, exp))
}

private final class LiveShoppingCart[F[_]: GenUUID: MonadThrow] private (
    items: Items[F],
    redis: RedisCommands[F, String, String],
    exp: ShoppingCartExpiration)
  extends ShoppingCart[F] {

  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit] =
    redis.hSet(
      userId.value.toString(),
      itemId.value.toString(),
      quantity.value.toString()
    ) *> redis.expire(userId.value.toString(), exp.value)

  def delete(userId: UserId): F[Unit] = redis.del(userId.value.toString())

  def get(userId: UserId): F[CartTotal] =
    redis.hGetAll(userId.value.toString()).flatMap { it =>
      it.toList
        .traverseFilter {
          case (k, v) =>
            for {
              id <- GenUUID[F].read[ItemId](k)
              qt <- ApThrow[F].catchNonFatal(Quantity(v.toInt))
              rs <- items.findById(id).map(_.map(i => CartItem(i, qt)))
            } yield rs
        }
        .map(cis => CartTotal(cis, calcTotal(cis)))
    }

  private def calcTotal(items: List[CartItem]): Money =
    USD(
      items.foldMap(x => x.item.price.value * x.quantity.value)
    )

  def removeItem(userId: UserId, itemId: ItemId): F[Unit] =
    redis.hDel(userId.value.toString(), itemId.value.toString)

  def update(userId: UserId, cart: Cart): F[Unit] =
    redis.hGetAll(userId.value.toString()).flatMap { items =>
      items.toList.traverse_ {
        case (k, _) =>
          GenUUID[F].read[ItemId](k).flatMap { iid =>
            cart.items.get(iid).traverse_ { q =>
              redis.hSet(userId.value.toString(), k, q.value.toString())
            }
          }
      } *> redis.expire(userId.value.toString(), exp.value)
    }

}
