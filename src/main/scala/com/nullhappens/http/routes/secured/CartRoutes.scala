package com.nullhappens.http.routes

import cats._
import cats.implicits._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl._
import org.http4s.server._

import com.nullhappens.http.auth.users._
import com.nullhappens.http.json._
import com.nullhappens.models._

final class CartRoutes[F[_]: Defer: JsonDecoder: Monad](
    shoppingCart: ShoppingCart[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/cart"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case GET -> Root as user =>
        Ok(shoppingCart.get(user.value.id))
      case ar @ POST -> Root as user =>
        ar.req
          .asJsonDecode[Cart]
          .flatMap { cart =>
            cart.items.toList.traverse {
              case (id, quantity) =>
                shoppingCart.add(user.value.id, id, quantity)
            } *> Created()
          }
      case ar @ PUT -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap { cart =>
          shoppingCart.update(user.value.id, cart)
        } *> Ok()
      case DELETE -> Root / UUIDVar(uuid) as user =>
        shoppingCart.removeItem(
          user.value.id,
          ItemId(uuid)
        ) *> NoContent()
    }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )

}
