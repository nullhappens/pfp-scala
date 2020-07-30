package com.nullhappens.services

import scala.concurrent.duration._

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import cats.{Monad, MonadError}
import io.chrisdavenport.log4cats.Logger
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.RetryPolicies._
import retry.{RetryDetails, _}
import squants.market.Money

import com.nullhappens.effect.Background
import com.nullhappens.models._

final class Checkout[F[_]: Background: Logger: MonadThrow: Timer](
    payments: Payments[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F]) {

  protected val retryPolicy = limitRetries[F](3) |+| exponentialBackoff[F](
    10.milliseconds
  )

  def processPayment(payment: Payment): F[PaymentId] = {
    val action = retryingOnAllErrors[PaymentId](
      policy = retryPolicy,
      onError = logError("Payments")
    )(payments.process(payment))

    action.adaptError {
      case e =>
        PaymentError(
          Option(e.getMessage).getOrElse("Unknown")
        )
    }
  }

  def createOrder(
      userId: UserId,
      paymentId: PaymentId,
      items: List[CartItem],
      total: Money
    ): F[OrderId] = {

    val action = retryingOnAllErrors[OrderId](
      policy = retryPolicy,
      onError = logError("Order")
    )(orders.create(userId, paymentId, items, total))

    def bgAction(fa: F[OrderId]): F[OrderId] =
      fa.adaptError {
          case e => OrderError(Option(e.getMessage).getOrElse("Unknown"))
        }
        .onError {
          case _ =>
            Logger[F].error(s"Failed to create order for $paymentId") *>
              Background[F].schedule(bgAction(fa), 1.hour)
        }
    bgAction(action)
  }

  def checkout(userId: UserId, card: Card): F[OrderId] =
    shoppingCart
      .get(userId)
      .ensure(EmptyCartError)(_.items.nonEmpty)
      .flatMap {
        case CartTotal(items, total) =>
          for {
            pid <- processPayment(Payment(userId, total, card))
            order <- createOrder(userId, pid, items, total)
            _ <- shoppingCart.delete(userId).attempt.void
          } yield order
      }

  def logError(action: String)(e: Throwable, details: RetryDetails): F[Unit] =
    details match {
      case GivingUp(totalRetries, _) =>
        Logger[F].error(e)(s"Giving up on $action after $totalRetries retries")
      case WillDelayAndRetry(_, retriesSoFar, _) =>
        Logger[F].error(e)(
          s"Failed on $action.  We have retried $retriesSoFar times."
        )
    }
}
