package com.nullhappens.http.clients

import cats.implicits._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.{JsonDecoder, _}
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl

import com.nullhappens.effect._
import com.nullhappens.http.json._
import com.nullhappens.models.PaymentId
import com.nullhappens.services.{Payment, PaymentError}

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

class LivePaymentClient[F[_]: JsonDecoder: BracketThrow](client: Client[F])
  extends PaymentClient[F]
  with Http4sClientDsl[F] {
  private val baseUri = "http://localhost:8080/api/v1"

  def process(payment: Payment): F[com.nullhappens.models.PaymentId] =
    Uri
      .fromString(baseUri + "/payments")
      .liftTo[F]
      .flatMap { uri =>
        POST(payment, uri).flatMap { req =>
          client.run(req).use { r =>
            if (r.status == Status.Ok || r.status == Status.Conflict)
              r.asJsonDecode[PaymentId]
            else
              PaymentError(Option(r.status.reason).getOrElse("Unknown"))
                .raiseError[F, PaymentId]
          }
        }
      }
}
