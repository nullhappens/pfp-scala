package com.nullhappens.services

import scala.util.control.NoStackTrace

import squants.market.Money

import com.nullhappens.models._

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
