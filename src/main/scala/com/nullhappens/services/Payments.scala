package com.nullhappens.services

import com.nullhappens.models._
import squants.market.Money
import scala.util.control.NoStackTrace

trait Payments[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

final case class PaymentError(message: String) extends NoStackTrace

case class Payment(id: UserId, total: Money, card: Card)
case class Card(
    name: CardHolderName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CVV)
