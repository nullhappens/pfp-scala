package com.nullhappens.http.routes.secured

import cats.Defer
import cats.implicits._
import com.nullhappens.effect._
import com.nullhappens.models._
import com.nullhappens.http.auth.users.CommonUser
import com.nullhappens.services.Card
import com.nullhappens.services.Checkout
import com.nullhappens.http.json._
import com.nullhappens.services.PaymentError
import com.nullhappens.http.refined._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl._
import org.http4s._
import org.http4s.server._

final class CheckoutRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    checkoutService: Checkout[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/checkout"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as user =>
        ar.req.decodeR[Card] { card =>
          checkoutService
            .checkout(user.value.id, card)
            .flatMap(Created(_))
            .recoverWith {
              case CartNotFound(userId) =>
                NotFound(s"Cart not found for user: ${userId.value}")
              case EmptyCartError =>
                BadRequest("Shopping cart is empty!")
              case PaymentError(cause) =>
                BadRequest(cause)
              case OrderError(cause) =>
                BadRequest(cause)
            }
        }
    }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
