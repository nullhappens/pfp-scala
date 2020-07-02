package com.nullhappens.services

import com.nullhappens.models._
import squants.market.Money

trait Payments[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

case class Payment(id: UserId, total: Money, card: Card)
case class Card(
    name: CardHolderName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CVV)
