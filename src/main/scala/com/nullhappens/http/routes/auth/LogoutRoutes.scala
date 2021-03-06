package com.nullhappens.http.routes.auth

import cats._
import cats.implicits._
import dev.profunktor.auth.AuthHeaders
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }

import com.nullhappens.http.auth.users.CommonUser
import com.nullhappens.models.JwtToken
import com.nullhappens.services.Auth

final class LogoutRoutes[F[_]: Defer: Monad](auth: Auth[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root / "logout" as user =>
        AuthHeaders
          .getBearerToken(ar.req)
          .traverse_(t => auth.logout(JwtToken(t.value), user.value.name)) *> NoContent()
    }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
