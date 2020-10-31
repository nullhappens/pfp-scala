package com.nullhappens.http.routes.admin

import cats.Defer
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

import com.nullhappens.effect.MonadThrow
import com.nullhappens.http.auth.users.AdminUser
import com.nullhappens.http.json._
import com.nullhappens.http.refined._
import com.nullhappens.http.routes.CategoryParam
import com.nullhappens.models.Categories

final class AdminCategoryRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    categories: Categories[F])
  extends Http4sDsl[F] {

  private[admin] val prefixPath = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CategoryParam] { c =>
          Created(categories.create(c.toDomain))
        }
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
