package com.nullhappens.http.routes

import cats.implicits._
import cats.effect.implicits._
import cats.Defer
import cats.Monad
import org.http4s.dsl.Http4sDsl
import com.nullhappens.models.Categories
import org.http4s.HttpRoutes
import org.http4s.server.Router
import com.nullhappens.http.json._

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
