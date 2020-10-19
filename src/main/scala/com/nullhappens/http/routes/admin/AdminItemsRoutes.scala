package com.nullhappens.http.routes.admin

import cats.Defer
import cats.implicits._
import org.http4s.AuthedRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl._
import org.http4s.HttpRoutes
import org.http4s.server.AuthMiddleware
import org.http4s.server.Router

import com.nullhappens.effect.MonadThrow
import com.nullhappens.http.auth.users.AdminUser
import com.nullhappens.http.json._
import com.nullhappens.http.refined._
import com.nullhappens.http.routes.BrandParam
import com.nullhappens.http.CreateItemParam
import com.nullhappens.http.UpdateItemParam
import com.nullhappens.models.Brands
import com.nullhappens.models.Items

final class AdminItemRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    items: Items[F])
  extends Http4sDsl[F] {

  private[admin] val prefixPath = "/items"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CreateItemParam] { item =>
          Created(items.create(item.toDomain))
        }

      case ar @ PUT -> Root as _ =>
        ar.req.decodeR[UpdateItemParam] { item =>
          Ok(items.update(item.toDomain))
        }
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
