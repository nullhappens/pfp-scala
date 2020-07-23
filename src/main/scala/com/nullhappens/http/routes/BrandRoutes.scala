package com.nullhappens.http.routes

import cats.Defer
import cats.Monad
import com.nullhappens.models.Brands
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes
import com.nullhappens.http.json._

final class BrandRoutes[F[_]: Defer: Monad](brands: Brands[F])
  extends Http4sDsl[F] {

  private[routes] val prefixPath: String = "/brands"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root => Ok(brands.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
