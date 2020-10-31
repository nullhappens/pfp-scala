package com.nullhappens.http.routes.auth

import cats.Defer
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import com.nullhappens.effect.MonadThrow
import com.nullhappens.http.auth._
import com.nullhappens.http.json._
import com.nullhappens.http.refined._
import com.nullhappens.services.Auth

final class LoginRoutes[F[_]: Defer: JsonDecoder: MonadThrow](auth: Auth[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .recoverWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
