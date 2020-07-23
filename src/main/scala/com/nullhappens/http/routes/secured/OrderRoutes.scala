package com.nullhappens.http.routes.secured

import cats._
import com.nullhappens.models._
import org.http4s.dsl._
import org.http4s._
import com.nullhappens.http.auth.users.CommonUser
import com.nullhappens.http.json._
import org.http4s.dsl.impl._
import org.http4s.server._

final class OrderRoutes[F[_]: Defer: Monad](orders: Orders[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user =>
      Ok(orders.findBy(user.value.id))
    case GET -> Root / UUIDVar(orderId) as user =>
      Ok(orders.get(user.value.id, OrderId(orderId)))
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
