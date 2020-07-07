package com.nullhappens.services

import com.nullhappens.models._
import squants.market.Money

trait Payments[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

//TODO: I added this here, the book doesnt have this
final case class PaymentError(message: String) extends Exception

case class Payment(id: UserId, total: Money, card: Card)
case class Card(
    name: CardHolderName,
    number: CardNumber,
    expiration: CardExpiration,
    cvv: CVV)
