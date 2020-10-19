package com.nullhappens.http.routes

import cats.effect.implicits._
import cats.implicits._
import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import com.nullhappens.http.json._
import com.nullhappens.models.Categories

final class CategoryRoutes[F[_]: Defer: Monad](categories: Categories[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root => Ok(categories.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
