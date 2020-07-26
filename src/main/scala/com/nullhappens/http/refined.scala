package com.nullhappens.http

import cats.implicits._
import org.http4s.circe._
import io.circe._
import org.http4s.dsl.Http4sDsl
import com.nullhappens.effect.MonadThrow
import org.http4s.Request
import org.http4s.Response

object refined {
  implicit class RefinedRequestDecoder[F[_]: JsonDecoder: MonadThrow](
      req: Request[F])
    extends Http4sDsl[F] {

    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") =>
              BadRequest(c.getMessage)
            case _ => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }
  }
}
