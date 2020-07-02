package com.nullhappens.services

import cats.implicits._
import cats.Monad
import com.nullhappens.models._
import cats.MonadError

final class Checkout[F[_]](
    payments: Payments[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F]
  )(implicit ev: MonadError[F, Throwable]) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart <- shoppingCart.get(userId)
      paymentId <- payments.process(
        Payment(userId, cart.total, card)
      )
      orderId <- orders.create(userId, paymentId, cart.items, cart.total)
      _ <- shoppingCart.delete(userId).attempt.void
    } yield orderId

  // private def logError(action: String)(e: Throwable, details: RetryDetails) =
  //   ???
}
