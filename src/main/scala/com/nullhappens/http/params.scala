package com.nullhappens.http

import cats.implicits._
import org.http4s._
import eu.timepit.refined._
import eu.timepit.refined.api.Validate
import eu.timepit.refined.api.Refined
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object params {
  implicit def coercibleQueryParamDecoder[
      A: Coercible[B, *],
      B: QueryParamDecoder
    ]: QueryParamDecoder[A] =
    QueryParamDecoder[B].map(_.coerce[A])

  implicit def refinedQueryParamDecoder[T: QueryParamDecoder, P](
      implicit ev: Validate[T, P]
    ): QueryParamDecoder[T Refined P] =
    QueryParamDecoder[T].emap(refineV[P](_).leftMap(m => ParseFailure(m, m)))
}
