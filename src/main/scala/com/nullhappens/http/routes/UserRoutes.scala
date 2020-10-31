package com.nullhappens.http.routes

import cats.Defer
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import com.nullhappens.effect.MonadThrow
import com.nullhappens.http.auth.{CreateUser, _}
import com.nullhappens.http.json._
import com.nullhappens.http.refined._
import com.nullhappens.services.Auth

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](auth: Auth[F])
  extends Http4sDsl[F] {
  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      req.decodeR[CreateUser] { user =>
        auth
          .newUser(user.username.toDomain, user.password.toDomain)
          .flatMap(Created(_))
          .recoverWith {
            case UserNameInUse(u) => Conflict(u.value)
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
